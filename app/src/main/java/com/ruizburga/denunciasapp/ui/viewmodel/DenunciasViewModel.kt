package com.ruizburga.denunciasapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonParseException
import com.ruizburga.denunciasapp.data.model.ActualizarDenunciaRequest
import com.ruizburga.denunciasapp.data.model.Denuncia
import com.ruizburga.denunciasapp.data.model.CrearDenunciaRequest
import com.ruizburga.denunciasapp.data.repository.DenunciaRepository
import com.ruizburga.denunciasapp.data.repository.RetrofitDenunciaRepository
import com.ruizburga.denunciasapp.ui.state.ContentState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

class DenunciasViewModel(
    private val repository: DenunciaRepository = RetrofitDenunciaRepository()
) : ViewModel() {
    private var nextOffset = 0
    private var listRequestInProgress = false
    private var createRequestInProgress = false
    private var searchRequestInProgress = false
    private var attendanceRequestInProgress = false
    private var detailRequestToken = 0
    private val createdDuringSession = mutableListOf<Denuncia>()

    private val _listaState = MutableStateFlow<ContentState<List<Denuncia>>>(ContentState.Empty)
    val listaState: StateFlow<ContentState<List<Denuncia>>> = _listaState.asStateFlow()

    private val _listaRequestCompleted = MutableStateFlow(false)
    val listaRequestCompleted: StateFlow<Boolean> = _listaRequestCompleted.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    private val _paginationError = MutableStateFlow<String?>(null)
    val paginationError: StateFlow<String?> = _paginationError.asStateFlow()

    private val _detalleState = MutableStateFlow<ContentState<Denuncia>>(ContentState.Empty)
    val detalleState: StateFlow<ContentState<Denuncia>> = _detalleState.asStateFlow()

    private val _busquedaState = MutableStateFlow<ContentState<List<Denuncia>>>(ContentState.Empty)
    val busquedaState: StateFlow<ContentState<List<Denuncia>>> = _busquedaState.asStateFlow()

    private val _operacionState = MutableStateFlow<ContentState<Denuncia>>(ContentState.Empty)
    val operacionState: StateFlow<ContentState<Denuncia>> = _operacionState.asStateFlow()

    private val _atencionState = MutableStateFlow<ContentState<Denuncia>>(ContentState.Empty)
    val atencionState: StateFlow<ContentState<Denuncia>> = _atencionState.asStateFlow()

    private val _atendiendoId = MutableStateFlow<String?>(null)
    val atendiendoId: StateFlow<String?> = _atendiendoId.asStateFlow()

    fun cargarDenuncias(offset: Int = 0, limit: Int = 5) {
        if (listRequestInProgress) return
        listRequestInProgress = true
        nextOffset = offset
        _hasMore.value = true
        _paginationError.value = null
        viewModelScope.launch {
            _listaState.value = ContentState.Loading
            try {
                val result = withContext(Dispatchers.IO) { repository.listar(offset, limit) }
                Log.i(LOG_TAG, "GET /denuncias/ succeeded: ${result.size} records")
                nextOffset = offset + result.size
                _hasMore.value = result.size >= limit
                val merged = mergeCreated(result)
                _listaState.value = if (merged.isEmpty()) ContentState.Empty else ContentState.Data(merged)
            } catch (error: Exception) {
                Log.e(LOG_TAG, "GET /denuncias/ failed (${error.javaClass.name}): ${error.message}", error)
                _listaState.value = ContentState.Error(errorMessage(error))
            } finally {
                listRequestInProgress = false
                _listaRequestCompleted.value = true
            }
        }
    }

    fun cargarSiguientesDenuncias(limit: Int = 5) {
        if (listRequestInProgress || !_hasMore.value || limit <= 0) return
        listRequestInProgress = true
        _isLoadingMore.value = true
        _paginationError.value = null
        val offset = nextOffset

        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) { repository.listar(offset, limit) }
                nextOffset = offset + result.size
                _hasMore.value = result.size >= limit

                val current = (_listaState.value as? ContentState.Data)?.value.orEmpty()
                val combined = (current + result).distinctBy { it.id }
                _listaState.value = if (combined.isEmpty()) ContentState.Empty else ContentState.Data(combined)
            } catch (error: Exception) {
                Log.e(LOG_TAG, "GET /denuncias/ pagination failed (${error.javaClass.name}): ${error.message}", error)
                _paginationError.value = errorMessage(error)
            } finally {
                listRequestInProgress = false
                _isLoadingMore.value = false
            }
        }
    }

    fun cargarDetalle(id: String) {
        val requestToken = ++detailRequestToken
        _detalleState.value = ContentState.Loading
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) { repository.obtener(id) }
                if (requestToken == detailRequestToken) {
                    _detalleState.value = ContentState.Data(result)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                if (requestToken == detailRequestToken) {
                    _detalleState.value = ContentState.Error(errorMessage(error))
                }
            }
        }
    }

    fun buscarPorDni(dni: String) {
        val cleanDni = dni.trim()
        if (searchRequestInProgress || !Regex("^[0-9]{8}$").matches(cleanDni)) return
        searchRequestInProgress = true
        _busquedaState.value = ContentState.Loading
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) { repository.buscarPorDni(cleanDni) }
                _busquedaState.value = if (result.isEmpty()) ContentState.Empty else ContentState.Data(result)
            } catch (error: Exception) {
                _busquedaState.value = ContentState.Error(errorMessage(error))
            } finally {
                searchRequestInProgress = false
            }
        }
    }

    fun crearDenuncia(denuncia: CrearDenunciaRequest) {
        if (createRequestInProgress || _operacionState.value == ContentState.Loading) return
        createRequestInProgress = true
        viewModelScope.launch {
            _operacionState.value = ContentState.Loading
            try {
                val result = try {
                    ContentState.Data(withContext(Dispatchers.IO) { repository.crear(denuncia) })
                } catch (error: Exception) {
                    ContentState.Error(errorMessage(error))
                }
                if (result is ContentState.Data) {
                    createdDuringSession.removeAll { it.id == result.value.id }
                    createdDuringSession.add(0, result.value)
                    val current = (_listaState.value as? ContentState.Data)?.value.orEmpty()
                    val merged = mergeCreated(current)
                    _listaState.value = ContentState.Data(merged)
                    _hasMore.value = true
                }
                _operacionState.value = result
            } finally {
                createRequestInProgress = false
            }
        }
    }

    fun limpiarEstadoOperacion() {
        if (_operacionState.value != ContentState.Loading) {
            _operacionState.value = ContentState.Empty
        }
    }

    fun marcarComoAtendida(denuncia: Denuncia) {
        if (attendanceRequestInProgress || denuncia.estado != "1") return
        attendanceRequestInProgress = true
        _atendiendoId.value = denuncia.id
        _atencionState.value = ContentState.Loading

        viewModelScope.launch {
            try {
                val request = ActualizarDenunciaRequest(
                    dni = denuncia.dni,
                    fecha = denuncia.fecha,
                    tipo = denuncia.tipo,
                    direccion = denuncia.direccion,
                    descripcion = denuncia.descripcion,
                    estado = 0
                )
                val response = withContext(Dispatchers.IO) {
                    repository.actualizar(denuncia.id, request)
                }
                val updated = response.copy(estado = "0")
                updateLoadedDenuncia(updated)
                _atencionState.value = ContentState.Data(updated)
            } catch (error: Exception) {
                _atencionState.value = ContentState.Error(errorMessage(error))
            } finally {
                attendanceRequestInProgress = false
                _atendiendoId.value = null
            }
        }
    }

    fun limpiarEstadoAtencion() {
        if (_atencionState.value != ContentState.Loading) {
            _atencionState.value = ContentState.Empty
        }
    }

    private fun runOperation(request: suspend () -> Denuncia) {
        if (_operacionState.value == ContentState.Loading) return
        viewModelScope.launch {
            _operacionState.value = ContentState.Loading
            _operacionState.value = requestState(request)
        }
    }

    private suspend fun requestState(request: suspend () -> Denuncia): ContentState<Denuncia> = try {
        ContentState.Data(withContext(Dispatchers.IO) { request() })
    } catch (error: Exception) {
        ContentState.Error(error.message ?: "No se pudo completar la operación.")
    }

    private fun errorMessage(error: Exception): String = when (error) {
        is HttpException -> "La API respondió con HTTP ${error.code()}. Revisa el endpoint y la respuesta del servidor."
        is JsonParseException, is IllegalStateException -> "La API respondió, pero el JSON no coincide con el formato esperado por la aplicación."
        is SocketTimeoutException -> "La API tardó demasiado en responder. Intenta nuevamente."
        is IOException -> "No se pudo completar la conexión con la API. Verifica que siga disponible."
        else -> "Error inesperado (${error.javaClass.simpleName}): ${error.localizedMessage ?: "sin detalle"}"
    }

    private fun mergeCreated(records: List<Denuncia>): List<Denuncia> =
        (createdDuringSession + records).distinctBy { it.id }

    private fun updateLoadedDenuncia(updated: Denuncia) {
        createdDuringSession.indices.forEach { index ->
            if (createdDuringSession[index].id == updated.id) createdDuringSession[index] = updated
        }
        val currentList = _listaState.value
        if (currentList is ContentState.Data) {
            _listaState.value = ContentState.Data(
                currentList.value.map { if (it.id == updated.id) updated else it }
            )
        }
        val currentSearch = _busquedaState.value
        if (currentSearch is ContentState.Data) {
            _busquedaState.value = ContentState.Data(
                currentSearch.value.map { if (it.id == updated.id) updated else it }
            )
        }
        val currentDetail = _detalleState.value
        if (currentDetail is ContentState.Data && currentDetail.value.id == updated.id) {
            _detalleState.value = ContentState.Data(updated)
        }
    }

    private companion object {
        const val LOG_TAG = "DenunciasApi"
    }
}
