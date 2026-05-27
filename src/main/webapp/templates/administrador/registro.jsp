
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <main class="container">
            <h2>Mora - Lote 01</h2> <form action="${pageContext.request.contextPath}/ServletLabor" method="POST">
                <input type="hidden" name="idCultivo" value="${param.idCultivo}">

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
                            <td><input type="number" name="dd" placeholder="DD" style="width:50px"></td>
                            <td><input type="number" name="mm" placeholder="MM" style="width:50px"></td>
                            <td><input type="number" name="aa" placeholder="AA" style="width:60px"></td>
                            <td><input type="text" name="labor"></td>
                            <td><input type="text" name="responsable"></td>
                            <td><input type="text" name="comentarios"></td>
                        </tr>
                    </tbody>
                </table>

                <button type="submit" class="btn--new" style="margin-top:20px;">Guardar registro</button>
            </form>
</main>
    </body>
</html>
