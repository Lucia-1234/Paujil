<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Asignar Roles - Finca El Paujil</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/views/asignar_rol.css">
</head>
<body>
    <main class="role-manager">

        <header class="role-manager__header">
            <a href="${pageContext.request.contextPath}/templates/administrador/menu_administrador.jsp"
               class="role-manager__back-link"
               aria-label="Volver al menú">
                <i class="fa-solid fa-arrow-left-long"></i>
            </a>
            <h1 class="role-manager__title">Asignar rol</h1>
        </header>

        <%-- Mensajes de resultado --%>
        <c:if test="${param.status == 'aceptado'}">
            <div class="feedback-message feedback-message--ok">
                <i class="fa-solid fa-circle-check"></i> Usuario aceptado correctamente.
            </div>
        </c:if>
        <c:if test="${param.status == 'denegado'}">
            <div class="feedback-message feedback-message--ok">
                <i class="fa-solid fa-circle-check"></i> Usuario denegado y eliminado.
            </div>
        </c:if>
        <c:if test="${param.status == 'error'}">
            <div class="feedback-message feedback-message--error">
                <i class="fa-solid fa-circle-exclamation"></i> Ocurrió un error. Intente nuevamente.
            </div>
        </c:if>

        <section class="role-manager__content">
            <ul class="request-list">
                <c:choose>
                    <c:when test="${empty usuariosPendientes}">
                        <li class="no-data">
                            <i class="fa-solid fa-circle-info" style="margin-right:6px;"></i>
                            No hay usuarios pendientes de aprobación.
                        </li>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="u" items="${usuariosPendientes}">
                            <li class="request-item" id="user-${u.idUsuario}">
                                <div class="request-item__user-info">
                                    <div class="request-item__avatar">
                                        <i class="fa-solid fa-user"></i>
                                    </div>
                                    <div class="request-item__details">
                                        <h2 class="request-item__name">${u.nombre}</h2>
                                        <p class="request-item__meta">
                                            <i class="fa-solid fa-envelope" style="width:14px;"></i> ${u.correo}
                                        </p>
                                        <p class="request-item__meta">
                                            <i class="fa-solid fa-phone" style="width:14px;"></i> ${u.telefono}
                                        </p>
                                        <p class="request-item__type">
                                            <i class="fa-solid fa-tag" style="width:14px;"></i>
                                            Rol solicitado: <strong>${u.rol}</strong>
                                        </p>
                                    </div>
                                </div>

                                <div class="request-item__actions">
                                    <form action="${pageContext.request.contextPath}/GestionarRoles" method="POST">
                                        <input type="hidden" name="id_usuario" value="${u.idUsuario}">
                                        <button type="submit" name="accion" value="aceptar"
                                                class="btn btn--accept">
                                            <i class="fa-solid fa-check"></i> Aceptar
                                        </button>
                                    </form>
                                    <form action="${pageContext.request.contextPath}/GestionarRoles" method="POST">
                                        <input type="hidden" name="id_usuario" value="${u.idUsuario}">
                                        <button type="submit" name="accion" value="denegar"
                                                class="btn btn--deny">
                                            <i class="fa-solid fa-xmark"></i> Negar
                                        </button>
                                    </form>
                                </div>
                            </li>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </ul>
        </section>

    </main>

    <script src="${pageContext.request.contextPath}/static/js/script.js"></script>
</body>
</html>
