

package paujil.basedatos;

//se importa la clase Connection del paquete java.sql
import java.sql.Connection;
// se importa el paquete DriverManager del paquete java.sql
import java.sql.DriverManager;

public class clase_Conexion {
    
    //se declara metodo en java llamado MetodoConectar que devolvera un objeto de tipo Connection
    public static Connection MetodoConectar(){
        
        //se declara una variable llamada con de tipo Connection y se le asigna el valor null inicialmente
        // null: la variable no apunta a ninguna conexión. Se inicializa vacía para luego asignarle un objeto real.
        Connection con = null;
        
        //Try catch para manejar errores 
        try {
        
            //carga en memoria la clase del driver jdbc de mysql 
            //@Class.forName le dice a la JVM que busque y cargue la clase indicada por su nombre completo
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            //se define el url de conexion y las credenciales que java usara para conectarse a mysql 
            String url      = "jdbc:mysql://localhost:3306/finca_pajuil";
            String usuario  = "root";
            String password = "#root2026"; 
            
            //se abre la conexion de la base de datos que se asigna a la variable con (de tipo Connection)
            //@DriverManager.getConnection:es el método que pide al DriverManager que use el driver JDBC de MySQL para conectarse.
            con = DriverManager.getConnection(
                    url,        //contiene la direccion de la base de datos 
                    usuario,       //usuario de la base de datos 
                    password    //contraseña del usuario
            );
            
            //imprime un mensaje de exito de conexion en la consola 
            System.out.println("Conexion a base de datos exitosa");
          
          //captura cualquier exepcion que ocurra en el bloque try 
        } catch ( Exception e){
            
            //imprime el mensaje de salida de errores
            System.err.println("Error:" +e.getMessage());
            
            //muestra el detalle completo del error incluyendo la pila de llamadas (stack trace)
            e.printStackTrace();
        }
        
        //devuelve el objeto de conexion (con) al lugar donde se llamo el metodo
        return con;
    }  

    //metodo principal del programa, intenta abrir la conexion con la base de datos  
    //si la conexion es exitosa se muestra el mensaje de exito 
    //si la conexion falla ejecuta el bloque catch 
    public static void main(String [] args){
        MetodoConectar();
    }
}
