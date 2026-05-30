<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/base.css">
    <title>Agregar Registro - Finca El Paujil</title>
</head>
<body>
<main class="page-wrapper">

    <header class="list-header">
        <a href="${pageContext.request.contextPath}/ServletCultivo"
           class="list-header__back" aria-label="Volver a cultivos">
            <i class="fa-solid fa-arrow-left-long"></i>
        </a>
        <h1 class="list-header__title">Agregar registro de cultivo</h1>
    </header>

    <div class="panel">
        <form action="${pageContext.request.contextPath}/ServletTrabajo" method="POST">
            <input type="hidden" name="idCultivo" value="${param.idCultivo}">

            <div style="background:var(--color-white); border-radius:var(--radius-card); overflow:hidden;">
                <table class="registro-table">
                    <thead>
                        <tr>
                            <th colspan="3">Fecha</th>
                            <th>Labor realizada</th>
                            <th>Responsables</th>
                            <th>Comentarios</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td>
                                <input type="number" name="dd" placeholder="DD"
                                       min="1" max="31"
                                       style="width:52px; border:none; background:var(--color-light-green-bg);
                                              border-radius:var(--radius-sm); padding:6px 8px;
                                              font-size:var(--font-size-sm); text-align:center;">
                            </td>
                            <td>
                                <input type="number" name="mm" placeholder="MM"
                                       min="1" max="12"
                                       style="width:52px; border:none; background:var(--color-light-green-bg);
                                              border-radius:var(--radius-sm); padding:6px 8px;
                                              font-size:var(--font-size-sm); text-align:center;">
                            </td>
                            <td>
                                <input type="number" name="aa" placeholder="AAAA"
                                       style="width:72px; border:none; background:var(--color-light-green-bg);
                                              border-radius:var(--radius-sm); padding:6px 8px;
                                              font-size:var(--font-size-sm); text-align:center;">
                            </td>
                            <td>
                                <input type="text" name="labor"
                                       style="width:100%; border:none; background:var(--color-light-green-bg);
                                              border-radius:var(--radius-sm); padding:6px 10px;
                                              font-size:var(--font-size-sm);">
                            </td>
                            <td>
                                <input type="text" name="responsable"
                                       style="width:100%; border:none; background:var(--color-light-green-bg);
                                              border-radius:var(--radius-sm); padding:6px 10px;
                                              font-size:var(--font-size-sm);">
                            </td>
                            <td>
                                <input type="text" name="comentarios"
                                       style="width:100%; border:none; background:var(--color-light-green-bg);
                                              border-radius:var(--radius-sm); padding:6px 10px;
                                              font-size:var(--font-size-sm);">
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>

            <div style="display:flex; justify-content:center; margin-top:var(--spacing-lg);">
                <button type="submit" class="btn btn--save-job">
                    <i class="fa-solid fa-floppy-disk"></i> Guardar registro
                </button>
            </div>
        </form>
    </div>
</main>
</body>
</html>
