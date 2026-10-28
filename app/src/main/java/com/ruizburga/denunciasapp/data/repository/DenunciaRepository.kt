package com.ruizburga.denunciasapp.data.repository

import com.ruizburga.denunciasapp.data.model.Denuncia
import com.ruizburga.denunciasapp.data.model.CrearDenunciaRequest
import com.ruizburga.denunciasapp.data.model.ActualizarDenunciaRequest
import com.ruizburga.denunciasapp.data.remote.DenunciasApi
import com.ruizburga.denunciasapp.data.remote.RetrofitProvider

interface DenunciaRepository {
    suspend fun crear(denuncia: CrearDenunciaRequest): Denuncia
    suspend fun listar(offset: Int = 0, limit: Int = 5): List<Denuncia>
    suspend fun obtener(id: String): Denuncia
    suspend fun buscarPorDni(dni: String): List<Denuncia>
    suspend fun actualizar(id: String, denuncia: ActualizarDenunciaRequest): Denuncia
}

class RetrofitDenunciaRepository(
    private val api: DenunciasApi = RetrofitProvider.denunciasApi
) : DenunciaRepository {
    override suspend fun crear(denuncia: CrearDenunciaRequest) = api.crearDenuncia(denuncia)
    override suspend fun listar(offset: Int, limit: Int) = api.listarDenuncias(offset, limit)
    override suspend fun obtener(id: String) = api.obtenerDenuncia(id)
    override suspend fun buscarPorDni(dni: String) = api.buscarPorDni(dni)
    override suspend fun actualizar(id: String, denuncia: ActualizarDenunciaRequest) = api.actualizarDenuncia(id, denuncia)
}
