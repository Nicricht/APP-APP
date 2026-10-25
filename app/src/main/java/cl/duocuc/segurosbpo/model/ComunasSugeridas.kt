package cl.duocuc.segurosbpo.model

import java.text.Normalizer
import java.util.Locale

/** Opciones frecuentes; la búsqueda admite una comuna no incluida en el listado. */
object ComunasSugeridas {
    val todas = listOf(
        "Santiago", "Maipú", "Puente Alto", "La Florida", "Las Condes", "Ñuñoa",
        "Providencia", "Quilicura", "Pudahuel", "San Bernardo", "Peñalolén", "La Pintana",
        "Estación Central", "Independencia", "Conchalí", "Recoleta", "Huechuraba",
        "Vitacura", "Lo Barnechea", "La Reina", "Macul", "San Miguel", "San Joaquín",
        "La Cisterna", "La Granja", "San Ramón", "El Bosque", "Cerrillos",
        "Lo Espejo", "Pedro Aguirre Cerda", "Lo Prado", "Cerro Navia", "Renca",
        "Quinta Normal", "Padre Hurtado", "Peñaflor",
        "Talagante", "Isla de Maipo", "El Monte", "Melipilla", "Curacaví",
        "Lampa", "Colina", "Tiltil", "Buin", "Paine", "Calera de Tango",
        "Pirque", "San José de Maipo", "María Pinto", "Alhué", "San Pedro",
        "Arica", "Iquique", "Alto Hospicio", "Pozo Almonte", "Antofagasta",
        "Calama", "Tocopilla", "Copiapó", "Vallenar", "La Serena", "Coquimbo",
        "Ovalle", "Illapel", "Valparaíso", "Viña del Mar", "Quilpué", "Villa Alemana",
        "Concón", "Quillota", "La Calera", "San Antonio", "Los Andes", "San Felipe",
        "Rancagua", "Machalí", "Rengo", "San Fernando", "Santa Cruz",
        "Talca", "Curicó", "Linares", "Cauquenes", "Chillán", "San Carlos",
        "Concepción", "Talcahuano", "Hualpén", "San Pedro de la Paz", "Coronel",
        "Los Ángeles", "Lota", "Tomé", "Temuco", "Padre Las Casas", "Villarrica",
        "Pucón", "Angol", "Valdivia", "La Unión", "Osorno", "Puerto Montt",
        "Puerto Varas", "Ancud", "Castro", "Coyhaique", "Puerto Aysén",
        "Punta Arenas", "Puerto Natales", "Porvenir"
    ).distinct()

    private fun normalizar(texto: String) = Normalizer.normalize(
        texto.trim().lowercase(Locale.ROOT), Normalizer.Form.NFD
    ).replace(Regex("\\p{M}+"), "")

    fun buscar(texto: String): List<String> {
        val consulta = normalizar(texto)
        return todas.filter { normalizar(it).contains(consulta) }.take(25)
    }
}
