package com.paujil.modelo;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class biopreparado {

    private int idBiopreparado;
    private String nombre;
    private String descripcion;
    private double precio;
    private Date fechaCreacion;
    private Date fechaVencimiento;
    private String preparacion;
    private List<ingredienteBio> ingredientes;

    public biopreparado() {
        // Inicializa la lista para evitar NullPointerException al agregar ingredientes antes de setear datos
        this.ingredientes = new ArrayList<>();
    }

    // Constructor parametrizado usado desde el Servlet al procesar formularios HTTP con todos los campos del producto
    public biopreparado(String nombre, String descripcion, double precio,
                        Date fechaCreacion, Date fechaVencimiento, String preparacion) {
        // Asigna cada parametro recibido del Servlet al campo correspondiente del objeto
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.fechaCreacion = fechaCreacion;
        this.fechaVencimiento = fechaVencimiento;
        this.preparacion = preparacion;
        // Garantiza que la lista de ingredientes exista desde el momento de creacion del objeto
        this.ingredientes = new ArrayList<>();
    }

    // ── Clase interna estatica: encapsula un ingrediente individual dentro del biopreparado ──────────────
    // Estatica para poder instanciarse sin necesitar una instancia del biopreparado contenedor
    public static class ingredienteBio {

        // Identificador del ingrediente en base de datos; se asigna tras consulta o insercion
        private int idIngrediente;
        // Nombre del ingrediente biologico o quimico utilizado en la formula
        private String nombre;
        // Cantidad numerica del ingrediente; double permite fracciones como 0.5 o 1.75
        private double cantidad;
        // Unidad de medida asociada a la cantidad (ej: "ml", "g", "kg", "L")
        private String unidad;

        // Constructor vacio requerido para instanciacion por reflexion o asignacion posterior via setters
        public ingredienteBio() {}

        // Constructor usado desde el Servlet para crear ingredientes a partir de datos del formulario
        public ingredienteBio(String nombre, double cantidad, String unidad) {
            this.nombre = nombre;
            this.cantidad = cantidad;
            this.unidad = unidad;
        }

        // Expone el id del ingrediente para lecturas; util al mapear resultados de consultas SQL
        public int getIdIngrediente() { return idIngrediente; }
        // Permite asignar el id generado por la base de datos tras una insercion
        public void setIdIngrediente(int idIngrediente) { this.idIngrediente = idIngrediente; }

        // Retorna el nombre del ingrediente para mostrar en vistas o validaciones
        public String getNombre() { return nombre; }
        // Actualiza el nombre del ingrediente; util en operaciones de edicion
        public void setNombre(String nombre) { this.nombre = nombre; }

        // Expone la cantidad para calculos de formulacion o visualizacion
        public double getCantidad() { return cantidad; }
        // Permite modificar la cantidad en escenarios de ajuste de receta
        public void setCantidad(double cantidad) { this.cantidad = cantidad; }

        // Retorna la unidad de medida para acompañar el valor numerico en la UI
        public String getUnidad() { return unidad; }
        // Permite cambiar la unidad si se requiere conversion o correccion de datos
        public void setUnidad(String unidad) { this.unidad = unidad; }
    }

    // ── Accesores del biopreparado: permiten lectura y escritura controlada de cada campo privado ─────────

    // Retorna el id de base de datos; usado para operaciones UPDATE, DELETE o como clave en joins
    public int getIdBiopreparado() { return idBiopreparado; }
    // Asigna el id tras recuperar el registro de la base de datos o despues de un INSERT con generated key
    public void setIdBiopreparado(int idBiopreparado) { this.idBiopreparado = idBiopreparado; }

    // Expone el nombre comercial para mostrar en listados y formularios
    public String getNombre() { return nombre; }
    // Permite actualizar el nombre en flujos de edicion del producto
    public void setNombre(String nombre) { this.nombre = nombre; }

    // Retorna la descripcion para renderizar en vistas de detalle del producto
    public String getDescripcion() { return descripcion; }
    // Actualiza la descripcion; util al editar informacion del producto desde el Servlet
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    // Expone el precio para calculos de venta, reportes o presentacion en la UI
    public double getPrecio() { return precio; }
    // Permite ajustar el precio del producto sin necesidad de recrear el objeto
    public void setPrecio(double precio) { this.precio = precio; }

    // Retorna la fecha de fabricacion; usada para calcular antiguedad o validar vigencia
    public Date getFechaCreacion() { return fechaCreacion; }
    // Asigna la fecha de creacion al mapear un ResultSet de base de datos o procesar un formulario
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    // Retorna la fecha de vencimiento; critica para logica de alertas y filtros de productos vigentes
    public Date getFechaVencimiento() { return fechaVencimiento; }
    // Permite actualizar la fecha de vencimiento ante cambios de lote o correcciones de datos
    public void setFechaVencimiento(Date fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    // Expone las instrucciones de preparacion para mostrar en ficha tecnica o guias de uso
    public String getPreparacion() { return preparacion; }
    // Actualiza el procedimiento de elaboracion; util en edicion de formula del producto
    public void setPreparacion(String preparacion) { this.preparacion = preparacion; }

    // Retorna la lista completa de ingredientes asociados; usada para iterar en vistas o calculos de costo
    public List<ingredienteBio> getIngredientes() { return ingredientes; }
    // Reemplaza toda la coleccion de ingredientes; util al actualizar la formula completa desde el Servlet
    public void setIngredientes(List<ingredienteBio> ingredientes) { this.ingredientes = ingredientes; }
}