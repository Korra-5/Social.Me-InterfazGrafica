package com.example.socialme_interfazgrafica.utils

object ErrorUtils {
    fun parseErrorMessage(errorMsg: String): String {
        return when {
            // ERRORES DE CONEXIÓN
            errorMsg.contains("Tiempo de espera agotado") ->
                "La conexión ha tardado demasiado. Inténtalo de nuevo"

            errorMsg.contains("timeout") || errorMsg.contains("timed out") ->
                "Conexión fallida, inténtelo de nuevo"

            errorMsg.contains("Unable to resolve host") ||
                    errorMsg.contains("Failed to connect") ||
                    errorMsg.contains("No address associated") ->
                "Error de conexión, compruebe su internet"

            // ERRORES DE AUTENTICACIÓN
            errorMsg.contains("401") || errorMsg.contains("Unauthorized") ||
                    errorMsg.contains("credenciales") || errorMsg.contains("no existente") ->
                "Usuario o contraseña incorrectos"

            errorMsg.contains("403") || errorMsg.contains("Forbidden") ->
                "No tienes permisos para realizar esta acción"

            // ERRORES DE VERIFICACIÓN
            errorMsg.contains("Código de verificación incorrecto") ->
                "El código ingresado es incorrecto"

            errorMsg.contains("código de verificación ha expirado") ||
                    errorMsg.contains("El código de verificación ha expirado") ->
                "El código ha expirado. Solicita uno nuevo"

            errorMsg.contains("No se encontró código de verificación") ||
                    errorMsg.contains("No se encontraron datos de registro") ||
                    errorMsg.contains("No se encontraron datos de modificación") ->
                "Código no válido. Solicita uno nuevo"

            errorMsg.contains("No se pudo enviar el código de verificación") ->
                "Error al enviar el código. Verifica tu email"

            // ERRORES DE USUARIO - REGISTRO Y LOGIN
            errorMsg.contains("ya está registrado") || errorMsg.contains("ya está en uso") ->
                "Este usuario ya existe. Prueba con otro nombre"

            errorMsg.contains("email") && errorMsg.contains("ya está registrado") ->
                "Este email ya está registrado"

            errorMsg.contains("ya está registrado por otro usuario") ->
                "Este email ya está registrado por otro usuario"

            // ERRORES DE USUARIOS NO ENCONTRADOS (ESPECÍFICOS)
            errorMsg.contains("Usuario") && (errorMsg.contains("no encontrado") || errorMsg.contains("not found")) ->
                "Usuario no encontrado"

            errorMsg.contains("Usuario remitente") && errorMsg.contains("no encontrado") ->
                "Usuario remitente no encontrado"

            errorMsg.contains("Usuario destinatario") && errorMsg.contains("no encontrado") ->
                "Usuario destinatario no encontrado"

            errorMsg.contains("Usuario bloqueador") && errorMsg.contains("no encontrado") ->
                "Usuario no encontrado"

            errorMsg.contains("Usuario a bloquear") && errorMsg.contains("no encontrado") ->
                "Usuario a bloquear no encontrado"

            errorMsg.contains("Usuario bloqueado") && errorMsg.contains("no encontrado") ->
                "Usuario bloqueado no encontrado"

            errorMsg.contains("Usuario a eliminar no encontrado") ->
                "Usuario a eliminar no encontrado"

            errorMsg.contains("Usuario solicitante") && errorMsg.contains("no encontrado") ->
                "Usuario solicitante no encontrado"

            errorMsg.contains("Administrador con username") && errorMsg.contains("no encontrado") ->
                "El administrador especificado no existe"

            errorMsg.contains("no existe") && errorMsg.contains("usuario") ->
                "Usuario no encontrado"

            errorMsg.contains("no existente") ->
                "Usuario no encontrado"

            // ERRORES DE ADMINISTRADORES
            errorMsg.contains("Los administradores no pueden unirse a actividades") ->
                "Los administradores no pueden participar en actividades"

            errorMsg.contains("Los administradores no pueden unirse a comunidades") ->
                "Los administradores no pueden unirse a comunidades"

            errorMsg.contains("Los administradores no pueden bloquear usuarios") ->
                "Los administradores no pueden bloquear usuarios"

            errorMsg.contains("No puedes bloquear a administradores") ->
                "No puedes bloquear administradores"

            errorMsg.contains("No puedes enviar solicitudes de amistad a administradores") ->
                "No puedes enviar solicitudes a administradores"

            errorMsg.contains("Los administradores no pueden enviar solicitudes de amistad") ->
                "Los administradores no pueden enviar solicitudes"

            errorMsg.contains("Los administradores no pueden eliminar al creador") ->
                "No tienes permisos para eliminar este usuario"

            // ERRORES DE ACTIVIDADES
            errorMsg.contains("Esta actividad no existe") || errorMsg.contains("Actividad no existe") ->
                "Actividad no encontrada"

            errorMsg.contains("La actividad no existe") ->
                "Actividad no encontrada"

            errorMsg.contains("Ya estás participando en esta actividad") ->
                "Ya estás participando en esta actividad"

            errorMsg.contains("No te has unido a esta actividad") ->
                "No estás participando en esta actividad"

            errorMsg.contains("ya ha finalizado") || errorMsg.contains("que ya ha finalizado") ->
                "Esta actividad ya ha terminado"

            errorMsg.contains("sin pertenecer a la comunidad") ->
                "Debes unirte a la comunidad primero"

            errorMsg.contains("No tienes permisos para crear esta actividad") ->
                "No puedes crear actividades en esta comunidad"

            errorMsg.contains("Credenciales incorrectas") ->
                "El usuario o la contraseña son incorrectos"

            errorMsg.contains("Esta url es demasiado larga, pruebe con uno inferior a 100 caracteres") ->
                "La url es demasiado larga (máximo 100 caracteres)"

            errorMsg.contains("Este nombre es demasiado largo, pruebe con uno inferior a 40 caracteres")->
                "El nombre es demasiado largo (máximo 40 caracteres)\""

            errorMsg.contains("Ya existe una solicitud de amistad pendiente con este usuario")->
                "Ya existe una solicitud de amistad pendiente con este usuario"

            errorMsg.contains("El creador de la comunidad no puede ser añadido como administrador") ->
                "El creador de la comunidad no puede ser adminsitrador"

            errorMsg.contains("El usuario debe estar unido a la comunidad para ser administrador") ->
                "El usuario debe estar unido a la comunidad para ser administrador"

            errorMsg.contains("El nombre de la actividad no puede superar los 40 caracteres") ->
                "El nombre es demasiado largo (máximo 40 caracteres)"

            errorMsg.contains("La descrpicion no puede superar los 600 caracteres") ->
                "La descripción es demasiado larga (máximo 600 caracteres)"

            // ERRORES DE COMUNIDADES - NO ENCONTRADAS
            errorMsg.contains("Comunidad") && (errorMsg.contains("no encontrada") || errorMsg.contains("not found")) ->
                "Comunidad no encontrada"

            errorMsg.contains("El lugar no puede superar los 40 caracteres")->
                ("El lugar es demasiado largo (maximo 40 caracteres)")

            errorMsg.contains("Esta comunidad no existe") || errorMsg.contains("Comunidad no existe") ->
                "Comunidad no encontrada"

            errorMsg.contains("La comunidad no existe") ->
                "Comunidad no encontrada"

            errorMsg.contains("No existe esta comunidad") ->
                "Comunidad no encontrada"

            errorMsg.contains("Comunidad no encontrado") ->
                "Comunidad no encontrada"

            errorMsg.contains("Comunidad con URL") && errorMsg.contains("no encontrada") ->
                "Comunidad no encontrada"

            // ERRORES DE COMUNIDADES - DUPLICADAS
            errorMsg.contains("Comunidad existente") ->
                "Ya existe una comunidad con este nombre"

            errorMsg.contains("Ya existe una comunidad con la URL") ->
                "Esta URL ya está en uso. Prueba con otra"

            // ERRORES DE PARTICIPACIÓN EN COMUNIDADES
            errorMsg.contains("Ya estás unido") || errorMsg.contains("El usuario ya está unido") ->
                "Ya estás unido a esta comunidad"

            errorMsg.contains("No estás en esta comunidad") ->
                "No perteneces a esta comunidad"

            errorMsg.contains("El creador no puede abandonar la comunidad") ->
                "El creador no puede abandonar la comunidad"

            errorMsg.contains("no es miembro de esta comunidad") ->
                "No eres miembro de esta comunidad"

            errorMsg.contains("El usuario no es miembro de esta comunidad") ->
                "No eres miembro de esta comunidad"

            errorMsg.contains("no pertenece a esta comunidad") ->
                "El usuario no pertenece a esta comunidad"

            // ERRORES DE CÓDIGOS DE UNIÓN
            errorMsg.contains("codigo de union no es correcto") ->
                "Código de unión incorrecto"

            errorMsg.contains("es publica") ->
                "Esta comunidad es pública, no necesita código"

            // ERRORES DE PERMISOS EN COMUNIDADES
            errorMsg.contains("No tienes permisos para eliminar usuarios") ->
                "No tienes permisos para eliminar usuarios"

            errorMsg.contains("Solo el creador actual puede transferir") ->
                "Solo el creador puede transferir la propiedad"

            errorMsg.contains("debe ser miembro de la comunidad para convertirse en creador") ->
                "El usuario debe ser miembro de la comunidad"

            errorMsg.contains("No puedes transferir la propiedad a ti mismo") ->
                "No puedes transferirte la propiedad a ti mismo"

            // ERRORES DE LÍMITES Y VALIDACIONES
            errorMsg.contains("límite máximo de 3 comunidades") ->
                "Has alcanzado el límite máximo de 3 comunidades"

            errorMsg.contains("Este nombre es demasiado largo") ->
                "El nombre es demasiado largo (máximo 40 caracteres)"

            errorMsg.contains("Lo sentimos, la descripción no puede superar los 1000 caracteres") ->
                "La descripción es demasiado larga (máximo 1000 caracteres)"

            errorMsg.contains("Se requiere una foto de perfil") ->
                "Debes añadir una foto de perfil"

            // ERRORES DE INTERESES
            errorMsg.contains("Los intereses no pueden exceder los 25 caracteres") ->
                "Los intereses son demasiado largos (máximo 25 caracteres)"

            errorMsg.contains("Los intereses no pueden contener espacios") ->
                "Los intereses no pueden contener espacios"

            errorMsg.contains("Los intereses no pueden contener comas") ->
                "Los intereses no pueden contener comas"

            errorMsg.contains("Con el fin de facilitar su uso, los intereses no pueden contener espacios") ->
                "Los intereses no pueden contener espacios"

            // ERRORES DE FECHAS
            errorMsg.contains("La fecha de inicio debe ser anterior") ->
                "La fecha de inicio debe ser anterior a la de finalización"

            // ERRORES DE MENSAJES/CHAT
            errorMsg.contains("El mensaje no puede estar vacío") ->
                "El mensaje no puede estar vacío"

            errorMsg.contains("El mensaje no puede exceder los 500 caracteres") ->
                "El mensaje es demasiado largo (máximo 500 caracteres)"

            // ERRORES DE CONTRASEÑA
            errorMsg.contains("La contraseña no puede estar vacía") ->
                "La contraseña no puede estar vacía"

            errorMsg.contains("Las contraseñas no coinciden") ->
                "Las contraseñas no coinciden"

            errorMsg.contains("al menos 6 caracteres") ->
                "La contraseña debe tener al menos 6 caracteres"

            errorMsg.contains("debe ser diferente") ->
                "La nueva contraseña debe ser diferente"

            errorMsg.contains("Esta contraseña no es valida") ->
                "Contraseña actual incorrecta"

            // ERRORES DE BLOQUEO Y AMISTAD
            errorMsg.contains("Ya has bloqueado a este usuario") ->
                "Ya has bloqueado a este usuario"

            errorMsg.contains("No puedes bloquearte a ti mismo") ->
                "No puedes bloquearte a ti mismo"

            errorMsg.contains("No existe un bloqueo") ->
                "No has bloqueado a este usuario"

            errorMsg.contains("Ya existe una solicitud de amistad") ->
                "Ya existe una solicitud de amistad pendiente"

            errorMsg.contains("solicitud ya ha sido aceptada") ||
                    errorMsg.contains("Esta solicitud ya ha sido aceptada") ->
                "Esta solicitud ya fue aceptada"

            errorMsg.contains("No se puede cancelar una solicitud que ya ha sido aceptada") ->
                "No puedes cancelar una solicitud ya aceptada"

            errorMsg.contains("usuarios bloqueados") ||
                    errorMsg.contains("que te han bloqueado") ->
                "No puedes interactuar con usuarios bloqueados"

            errorMsg.contains("No puedes ver las actividades de este usuario") ->
                "No tienes permisos para ver las actividades de este usuario"

            // ERRORES DE SOLICITUDES DE AMISTAD
            errorMsg.contains("Solicitud de amistad con ID") && errorMsg.contains("no encontrada") ->
                "Solicitud de amistad no encontrada"

            // ERRORES DE CONFIGURACIÓN
            errorMsg.contains("El valor del radar debe ser un número válido") ->
                "Valor de radar inválido"

            errorMsg.contains("radar debe estar entre 10 y 100") ->
                "El radar debe estar entre 10 y 100 km"

            errorMsg.contains("Valor de privacidad inválido") ->
                "Configuración de privacidad inválida"

            // ERRORES DE ELIMINACIÓN
            errorMsg.contains("No se puede eliminar la cuenta mientras seas el creador") ->
                "No puedes eliminar tu cuenta siendo creador de comunidades"

            errorMsg.contains("No se encontraron participantes para la actividad") ->
                "No hay participantes en esta actividad"

            // ERRORES DE DENUNCIAS
            errorMsg.contains("Denuncia con ID") && errorMsg.contains("no encontrada") ->
                "Denuncia no encontrada"

            // ERRORES DE NOTIFICACIONES
            errorMsg.contains("Notificación no encontrada") ->
                "Notificación no encontrada"

            // ERRORES GEOGRÁFICOS
            errorMsg.contains("municipio") && errorMsg.contains("no válido") ->
                "El municipio ingresado no es válido"

            errorMsg.contains("provincia") && errorMsg.contains("no válida") ->
                "La provincia ingresada no es válida"

            errorMsg.contains("502") && errorMsg.contains("municipio") ->
                "Municipio no encontrado"

            errorMsg.contains("502") && errorMsg.contains("provincia") ->
                "Provincia no encontrada"

            errorMsg.contains("502") ->
                "Error de validación geográfica"

            // ERRORES DE CONTENIDO
            errorMsg.contains("contenido inapropiado") ->
                "El contenido contiene palabras no permitidas"

            // ERRORES DE GRIDFS Y ARCHIVOS
            errorMsg.contains("Failed to store file") ->
                "Error al guardar archivo"

            errorMsg.contains("File not found") ->
                "Archivo no encontrado"

            errorMsg.contains("Base64 content is empty") ->
                "Contenido de imagen vacío"

            errorMsg.contains("Invalid base64 encoding") ->
                "Formato de imagen inválido"

            //OTROS
            errorMsg.contains("La descripcion de un usuario no puede ser superior a 600 caracteres") ->
                "La descripcion de un usuario no puede ser superior a 600 caracteres"

            errorMsg.contains("El municipio especificado no existe en la provincia indicada")->
                "El municipio especificado no existe en la provincia indicada"

            errorMsg.contains("El municipio especificado no existe en la provincia indicada")->
                "El municipio especificado no existe en la provincia indicada"

            errorMsg.contains("La provincia no es válida") ->
                "La provincia no es válida"

            errorMsg.contains("Los apellidos de un usuario no pueden ser superiores a 60 caracteres") ->
                "Los apellidos de un usuario no pueden ser superiores a 60 caracteres"

            errorMsg.contains("El username de un usuario no puede ser superior a 30 caracteres") ->
                "El username de un usuario no puede ser superior a 30 caracteres"



            // ERRORES DE FORMATO
            errorMsg.contains("Formato de coordenadas inválido") ->
                "Coordenadas inválidas"

            // ERRORES GENERALES HTTP
            errorMsg.contains("404") ->
                "Recurso no encontrado"

            errorMsg.contains("500") ->
                "Error del servidor, inténtelo más tarde"

            // MENSAJE POR DEFECTO
            else -> {
                if (errorMsg.length > 80) {
                    "Error: ${errorMsg.substring(0, 80)}..."
                } else {
                    "Error: $errorMsg"
                }
            }
        }
    }
}