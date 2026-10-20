package cl.duocuc.miregistro.viewmodel

import androidx.lifecycle.ViewModel
import cl.duocuc.miregistro.model.Asegurado
import cl.duocuc.miregistro.model.Compania
import cl.duocuc.miregistro.model.FormularioUiState
import cl.duocuc.miregistro.model.TipoSiniestro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FormularioViewModel : ViewModel() {
    //state private y mutable
    private val _uiState = MutableStateFlow(FormularioUiState())
    //publica / solo lectura
    val uiState: StateFlow<FormularioUiState> = _uiState.asStateFlow()

    //eventos de los campos
    //S-02: identificacion simulada, el asegurado se elige de una lista
    fun onAseguradoSeleccionado(asegurado: Asegurado){
        _uiState.update { it.copy(asegurado = asegurado, reporteExitoso = false) }
    }

    fun onFechaCambia(valor: String){
        _uiState.update { it.copy(fechaOcurrencia = valor, errorFecha = null, reporteExitoso = false) }
    }

    fun onComunaCambia(valor: String){
        _uiState.update { it.copy(comuna = valor, errorComuna = null, reporteExitoso = false) }
    }

    fun onDescripcionCambia(valor: String){
        _uiState.update { it.copy(descripcion = valor, errorDescripcion = null, reporteExitoso = false) }
    }

    fun onCompaniaSeleccionada(compania: Compania){
        _uiState.update { it.copy(compania = compania, errorCompania = null, reporteExitoso = false) }
    }

    fun onTipoSeleccionado(tipo: TipoSiniestro){
        //si el siniestro es de hogar, la pregunta por terceros no corresponde
        val terceros = if (tipo.esVehiculo) _uiState.value.huboTerceros else false
        _uiState.update { it.copy(tipoSiniestro = tipo, huboTerceros = terceros, reporteExitoso = false) }
    }

    fun onTercerosCambia(valor: Boolean){
        _uiState.update { it.copy(huboTerceros = valor) }
    }

    fun onDatosCorrectosCambia(valor: Boolean){
        _uiState.update { it.copy(datosCorrectos = valor, errorDatos = null) }
    }

    fun onRegistrar(){
        val estado = _uiState.value
        //validaciones de campos
        //la fecha se escribe dia-mes-año (ej: 29-09-2026)
        val errorFecha = if(estado.fechaOcurrencia.length < 10 || !estado.fechaOcurrencia.contains("-"))
            "Escribe la fecha como dia-mes-año, por ejemplo 29-09-2026" else null

        val errorComuna = if (estado.comuna.isBlank()) "El campo es obligatorio" else null

        val errorDescripcion = if (estado.descripcion.isBlank()) "Cuentanos brevemente que paso" else null

        //RN-18: no se reportan siniestros nuevos de una compañia que ya no gestionamos
        val errorCompania = if (!estado.compania.activa)
            "Ya no gestionamos siniestros nuevos de esta compañia" else null

        val errorDatos = if(!estado.datosCorrectos) "Debes confirmar que los datos son correctos" else null

        val hayErrores = listOf(
            errorFecha, errorComuna, errorDescripcion, errorCompania, errorDatos
        ).any{it != null}

        //RN-06 y RF-03: el numero se entrega recien cuando el reporte queda registrado
        val numero = if (hayErrores) "" else "SIN-26-00" + estado.correlativo
        //el siguiente reporte lleva el numero que sigue
        val siguiente = if (hayErrores) estado.correlativo else estado.correlativo + 1

        _uiState.update {
            it.copy(
                errorFecha = errorFecha,
                errorComuna = errorComuna,
                errorDescripcion = errorDescripcion,
                errorCompania = errorCompania,
                errorDatos = errorDatos,
                reporteExitoso = !hayErrores,
                numeroSiniestro = numero,
                correlativo = siguiente
            )
        }
    }

    fun onLimpiar(){
        _uiState.value = FormularioUiState()
    }
}
