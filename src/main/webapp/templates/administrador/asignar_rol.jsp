<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Asignar Roles - Finca El Paujil</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/asignar_rol.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
</head>
<body>
    <main class="role-manager">
        <header class="role-manager__header">
            <a href="${pageContext.request.contextPath}/templates/administrador/menu_administrador.jsp"
               class="role-manager__back-link">
                <i class="fa-solid fa-arrow-left-long"></i>
            </a>
            <h1 class="role-manager__title">Asignar rol</h1>
        </header>

        <%-- Mensajes de resultado de la acción anterior --%>
        <c:if test="${param.status == 'aceptado'}">
            <div class="feedback-message feedback-message--ok">Usuario aceptado correctamente.</div>
        </c:if>
        <c:if test="${param.status == 'denegado'}">
            <div class="feedback-message feedback-message--ok">Usuario denegado y eliminado.</div>
        </c:if>
        <c:if test="${param.status == 'error'}">
            <div class="feedback-message feedback-message--error">Ocurrió un error. Intente nuevamente.</div>
        </c:if>

        <section class="role-manager__content">
            <ul class="request-list">
                <%-- Los datos vienen del GestionarRoles.doGet — nunca de DAO directo en JSP --%>
                <c:choose>
                    <c:when test="${empty usuariosPendientes}">
                        <li class="no-data">No hay usuarios pendientes de aprobación.</li>
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
                                        <p class="request-item__meta">${u.correo}</p>
                                        <p class="request-item__meta">${u.telefono}</p>
                                        <p class="request-item__type">Rol solicitado: ${u.rol}</p>
                                    </div>
                                </div>

                                <div class="request-item__actions">
                                    <form action="${pageContext.request.contextPath}/GestionarRoles" method="POST">
                                        <input type="hidden" name="id_usuario" value="${u.idUsuario}">
                                        <button type="submit" name="accion" value="aceptar" class="btn btn--accept">Aceptar</button>
                                    </form>

                                    <form action="${pageContext.request.contextPath}/GestionarRoles" method="POST">
                                        <input type="hidden" name="id_usuario" value="${u.idUsuario}">
                                        <button type="submit" name="accion" value="denegar" class="btn btn--deny">Negar</button>
                                    </form>
                                </div>
                            </li>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </ul>
        </section>
    </main>

    <script src="${pageContext.request.contextPath}/static/JS/scripts.js"></script>
</body>
</html>
