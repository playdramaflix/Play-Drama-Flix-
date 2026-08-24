package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.WatchHistoryEntity
import com.example.data.local.WatchlistEntity
import com.example.data.model.*
import com.example.data.repository.PlayDramaFlixRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

enum class BottomNavTab(val label: String) {
    HOME("Home"),
    SHORTS("Shorts"),
    SEARCH("Search"),
    WATCHLIST("My List"),
    VIP("VIP")
}

enum class PlayerDisplayMode {
    STANDALONE_PAGE,
    FULLSCREEN_LANDSCAPE,
    SHORTS_VERTICAL_REEL
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PlayDramaFlixRepository(application)

    // Contents UI state
    private val _contentsState = MutableStateFlow<UiState<List<ContentItemDto>>>(UiState.Loading)
    val contentsState: StateFlow<UiState<List<ContentItemDto>>> = _contentsState.asStateFlow()

    // Watch Details UI State
    private val _watchDetailState = MutableStateFlow<UiState<WatchDetailResponse>>(UiState.Loading)
    val watchDetailState: StateFlow<UiState<WatchDetailResponse>> = _watchDetailState.asStateFlow()

    // Notifications State
    private val _notifications = MutableStateFlow<List<NotificationItemDto>>(emptyList())
    val notifications: StateFlow<List<NotificationItemDto>> = _notifications.asStateFlow()

    // Navigation & Player State
    private val _currentTab = MutableStateFlow(BottomNavTab.HOME)
    val currentTab: StateFlow<BottomNavTab> = _currentTab.asStateFlow()

    private val _activeContent = MutableStateFlow<ContentItemDto?>(null)
    val activeContent: StateFlow<ContentItemDto?> = _activeContent.asStateFlow()

    private val _selectedEpisode = MutableStateFlow<EpisodeDto?>(null)
    val selectedEpisode: StateFlow<EpisodeDto?> = _selectedEpisode.asStateFlow()

    private val _selectedServer = MutableStateFlow<ServerDto?>(null)
    val selectedServer: StateFlow<ServerDto?> = _selectedServer.asStateFlow()

    private val _playerDisplayMode = MutableStateFlow(PlayerDisplayMode.STANDALONE_PAGE)
    val playerDisplayMode: StateFlow<PlayerDisplayMode> = _playerDisplayMode.asStateFlow()

    // Search query with debounce
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow("All")
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter.asStateFlow()

    private val _selectedLanguageFilter = MutableStateFlow("All")
    val selectedLanguageFilter: StateFlow<String> = _selectedLanguageFilter.asStateFlow()

    // Room DB Flows
    val continueWatchingList: StateFlow<List<WatchHistoryEntity>> = repository.continueWatchingFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchlist: StateFlow<List<WatchlistEntity>> = repository.watchlistFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isCurrentItemInWatchlist: StateFlow<Boolean> = _activeContent
        .flatMapLatest { item ->
            if (item != null) repository.isItemInWatchlist(item.slug)
            else flowOf(false)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        loadContents()
        loadNotifications()
    }

    fun loadContents() {
        viewModelScope.launch {
            _contentsState.value = UiState.Loading
            repository.getContents().fold(
                onSuccess = { list ->
                    _contentsState.value = UiState.Success(list)
                },
                onFailure = { err ->
                    _contentsState.value = UiState.Error(err.message ?: "Failed to load dramas")
                }
            )
        }
    }

    fun loadNotifications() {
        viewModelScope.launch {
            repository.getNotifications().fold(
                onSuccess = { list -> _notifications.value = list },
                onFailure = {}
            )
        }
    }

    fun selectTab(tab: BottomNavTab) {
        _currentTab.value = tab
    }

    fun openContentDetail(content: ContentItemDto, playImmediately: Boolean = false) {
        _activeContent.value = content
        _selectedEpisode.value = null
        _selectedServer.value = null
        _watchDetailState.value = UiState.Loading
        viewModelScope.launch {
            repository.getWatchDetails(content.slug, content).fold(
                onSuccess = { details ->
                    _watchDetailState.value = UiState.Success(details)
                    val firstEp = details.episodes.firstOrNull()
                    _selectedEpisode.value = firstEp
                    val matchingServer = if (firstEp != null) details.servers.find { it.episodeId == firstEp.episodeId } else null
                    _selectedServer.value = matchingServer ?: details.servers.firstOrNull()

                    if (playImmediately) {
                        _playerDisplayMode.value = if (content.type == "shorts") {
                            PlayerDisplayMode.SHORTS_VERTICAL_REEL
                        } else {
                            PlayerDisplayMode.STANDALONE_PAGE
                        }
                    }
                },
                onFailure = { err ->
                    _watchDetailState.value = UiState.Error(err.message ?: "Failed to load watch details")
                }
            )
        }
    }

    fun selectEpisode(episode: EpisodeDto) {
        _selectedEpisode.value = episode
        val details = (_watchDetailState.value as? UiState.Success)?.data
        val matchingServer = details?.servers?.find { it.episodeId == episode.episodeId }
        if (matchingServer != null) {
            _selectedServer.value = matchingServer
        }
    }

    fun selectServer(server: ServerDto) {
        _selectedServer.value = server
    }

    fun setPlayerDisplayMode(mode: PlayerDisplayMode) {
        _playerDisplayMode.value = mode
    }

    fun closePlayerOrDetail() {
        _activeContent.value = null
        _selectedEpisode.value = null
        _playerDisplayMode.value = PlayerDisplayMode.STANDALONE_PAGE
        if (_currentTab.value == BottomNavTab.SHORTS) {
            _currentTab.value = BottomNavTab.HOME
        }
    }

    fun toggleWatchlistCurrentItem() {
        val item = _activeContent.value ?: return
        val currentInList = isCurrentItemInWatchlist.value
        viewModelScope.launch {
            repository.toggleWatchlist(item, currentInList)
        }
    }

    fun saveProgress(progressMs: Long, totalDurationMs: Long) {
        val content = _activeContent.value ?: return
        val ep = _selectedEpisode.value ?: return
        viewModelScope.launch {
            repository.saveWatchProgress(content, ep, progressMs, totalDurationMs)
        }
    }

    fun clearWatchHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String) {
        _selectedCategoryFilter.value = category
    }

    fun setLanguageFilter(language: String) {
        _selectedLanguageFilter.value = language
    }
}
