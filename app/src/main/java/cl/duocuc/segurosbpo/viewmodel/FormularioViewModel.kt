package cl.duocuc.segurosbpo.viewmodel

import androidx.lifecycle.ViewModel
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.Compania
import cl.duocuc.segurosbpo.model.FormularioUiState
import cl.duocuc.segurosbpo.model.TipoSiniestro
import cl.duocuc.segurosbpo.model.ValidadorSiniestro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FormularioViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FormularioUiState())

    val uiState: StateFlow<FormularioUiState> = _uiState.asStateFlow()


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

        val terceros = if (tipo.esVehiculo) _uiState.value.huboTerceros else false
        _uiState.update { it.copy(tipoSiniestro = tipo, huboTerceros = terceros, reporteExitoso = false) }
    }

    fun onTercerosCambia(valor: Boolean){
        _uiState.update { it.copy(huboTerceros = valor, reporteExitoso = false) }
    }

    fun onDatosCorrectosCambia(valor: Boolean){
        _uiState.update { it.copy(datosCorrectos = valor, errorDatos = null, reporteExitoso = false) }
    }

    fun onRegistrar(): Boolean {
        val estado = _uiState.value
        if (estado.reporteExitoso) return false


        val errorFecha = ValidadorSiniestro.errorFecha(estado.fechaOcurrencia)
        val errorComuna = ValidadorSiniestro.errorComuna(estado.comuna)
        val errorDescripcion = ValidadorSiniestro.errorDescripcion(estado.descripcion)
        val errorCompania = ValidadorSiniestro.errorCompania(estado.compania)

        val errorDatos = if(!estado.datosCorrectos) "Debes confirmar que los datos son correctos" else null

        val hayErrores = listOf(
            errorFecha, errorComuna, errorDescripcion, errorCompania, errorDatos
        ).any{it != null}

        _uiState.update {
            it.copy(
                errorFecha = errorFecha,
                errorComuna = errorComuna,
                errorDescripcion = errorDescripcion,
                errorCompania = errorCompania,
                errorDatos = errorDatos,
                reporteExitoso = false,
                numeroSiniestro = ""
            )
        }
        return !hayErrores
    }

    fun confirmarRegistro(numero: String) {
        _uiState.update { it.copy(reporteExitoso = true, numeroSiniestro = numero) }
    }

    fun seleccionarAsegurado(asegurado: Asegurado) {
        _uiState.update { it.copy(asegurado = asegurado) }
    }

    fun onLimpiar(){
        _uiState.value = FormularioUiState(asegurado = _uiState.value.asegurado)
    }
}
