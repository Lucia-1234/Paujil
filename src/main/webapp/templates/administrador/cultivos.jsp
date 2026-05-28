<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.paujil.dao.CultivoDao, com.paujil.modelo.cultivo, java.util.List"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/CSS/Styles.css">
    <title>Gestión de Cultivos - Finca El Paujil</title>
</head>
<body>
    <main class="container">

        <%-- Mensajes de estado tras operaciones del servlet --%>
        <% String status = request.getParameter("status"); %>
        <% if ("success".equals(status)) { %>
            <div class="feedback-message feedback-message--ok">Operación realizada correctamente.</div>
        <% } else if ("error".equals(status)) { %>
            <div class="feedback-message feedback-message--error">Ocurrió un error. Intente nuevamente.</div>
        <% } else if ("eliminado".equals(status)) { %>
            <div class="feedback-message feedback-message--ok">Cultivo eliminado correctamente.</div>
        <% } %>

        <section class="crop-list">
            <%
                CultivoDao dao = new CultivoDao();
                List<cultivo> lista = dao.listarCultivos();
                if (lista.isEmpty()) {
            %>
                <article class="crop-card">
                    <p>No hay cultivos registrados.</p>
                </article>
            <%
                } else {
                    for (cultivo c : lista) {
            %>
                <article class="crop-card">
                    <div class="crop-card__content">
                        <h2 class="crop-card__title"><%= c.getNombreCultivo() %></h2>
                        <p class="crop-card__description">Tipo: <%= c.getTipoCultivo() != null ? c.getTipoCultivo() : "—" %></p>
                        <p class="crop-card__dates">
                            Siembra: <%= c.getFechaSiembra() %> |
                            Cosecha: <%= c.getFechaCosecha() != null ? c.getFechaCosecha() : "Por definir" %>
                        </p>
                        <div class="crop-card__actions">
                            <%-- Botón editar: abre el modal y pre-llena los campos --%>
                            <button class="btn btn--edit"
                                    onclick="abrirModalEditar(
                                        '<%= c.getIdCultivo() %>',
                                        '<%= c.getNombreCultivo().replace("'", "\\'") %>',
                                        '<%= c.getTipoCultivo() != null ? c.getTipoCultivo().replace("'", "\\'") : "" %>',
                                        '<%= c.getFechaSiembra() %>',
                                        '<%= c.getFechaCosecha() != null ? c.getFechaCosecha() : "" %>'
                                    )">
                                <i class="fa-solid fa-pen"></i> Editar
                            </button>

                            <%-- Botón eliminar: abre modal de confirmación --%>
                            <button class="btn btn--delete"
                                    onclick="confirmarEliminar('<%= c.getIdCultivo() %>')">
                                <i class="fa-solid fa-trash"></i> Eliminar
                            </button>

                            <%-- Botón para registrar trabajo realizado en este cultivo --%>
                            <a href="${pageContext.request.contextPath}/templates/trabajador/agregar_trabajos.jsp?idCultivo=<%= c.getIdCultivo() %>"
                               class="btn btn--add">
                                <i class="fa-solid fa-plus"></i> Agregar registro
                            </a>
                        </div>
                    </div>
                </article>
            <%
                    }
                }
            %>

            <div class="crop-list__footer">
                <a href="${pageContext.request.contextPath}/templates/administrador/formulario_cultivo.jsp"
                   class="btn btn--new">
                    <i class="fa-solid fa-circle-plus"></i> Agregar cultivo
                </a>
            </div>
        </section>
    </main>

    <%-- Modal de edición --%>
    <div id="modalEditar" class="modal-overlay" style="display:none;">
        <div class="modal-content">
            <span class="modal-close" onclick="cerrarModalEditar()">&times;</span>
            <h2>Editar Cultivo</h2>
            <form action="${pageContext.request.contextPath}/ServletCultivo" method="POST">
                <%-- Sin campo 'accion': el servlet distingue edición por la presencia de 'id' --%>
                <input type="hidden" id="editId" name="id">

                <label>Nombre</label>
                <input type="text" id="editNombre" name="nombreCultivo" required>

                <label>Tipo</label>
                <input type="text" id="editTipo" name="tipoCultivo">

                <label>Fecha Siembra</label>
                <input type="date" id="editSiembra" name="fechaSiembra" required>

                <label>Fecha Cosecha</label>
                <input type="date" id="editCosecha" name="fechaCosecha">

                <button type="submit" class="btn btn--new">Guardar cambios</button>
            </form>
        </div>
    </div>

    <%-- Modal de confirmación de eliminación --%>
    <div id="modalConfirmacion" class="modal-overlay" style="display:none;">
        <div class="modal-content">
            <h2>¿Seguro que deseas eliminar este cultivo?</h2>
            <p>Esta acción no se puede deshacer.</p>
            <button class="btn" onclick="cerrarModal()">Cancelar</button>
            <a id="btnConfirmarEliminar"
               href="${pageContext.request.contextPath}/ServletCultivo?accion=eliminar&id="
               class="btn btn--delete">Eliminar</a>
        </div>
    </div>

    <script src="${pageContext.request.contextPath}/static/JS/scripts.js"></script>
</body>
</html>
