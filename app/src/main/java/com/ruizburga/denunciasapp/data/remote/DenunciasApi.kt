package com.ruizburga.denunciasapp.data.remote

import com.ruizburga.denunciasapp.data.model.Denuncia
import com.ruizburga.denunciasapp.data.model.CrearDenunciaRequest
import com.ruizburga.denunciasapp.data.model.ActualizarDenunciaRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface DenunciasApi {
    @POST("denuncias/")
    suspend fun crearDenuncia(@Body denuncia: CrearDenunciaRequest): Denuncia

    @GET("denuncias/")
    suspend fun listarDenuncias(
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 5
    ): List<Denuncia>

    @GET("denuncias/{id}")
    suspend fun obtenerDenuncia(@Path("id") id: String): Denuncia

    @GET("denuncias/ciudadano/{dni}")
    suspend fun buscarPorDni(@Path("dni") dni: String): List<Denuncia>

    @PUT("denuncias/{id}")
    suspend fun actualizarDenuncia(
        @Path("id") id: String,
        @Body denuncia: ActualizarDenunciaRequest
    ): Denuncia
}
