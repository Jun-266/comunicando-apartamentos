import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Servidor implements Runnable {

	private ArrayList<AdministradorConexiones> conexiones;
	private ServerSocket servidor;
	private boolean terminado;
	private ExecutorService piscina;

	public Servidor() {
		this.conexiones = new ArrayList<>();
		this.terminado = false;
	}

	@Override
	public void run() {
		try {
			servidor = new ServerSocket(9999);
			piscina = Executors.newCachedThreadPool();
			while (!terminado) {
				Socket cliente = servidor.accept();
				AdministradorConexiones conexion = new AdministradorConexiones(cliente);
				conexiones.add(conexion);
				piscina.execute(conexion);
			}
		} catch (IOException e) {
			apagar();
		}
	}

	public void transmitir(String mensaje) {
		for (AdministradorConexiones ac : conexiones) {
			if (ac != null | ac.getNombre().equalsIgnoreCase("porteria")) {
				ac.enviarMensaje(mensaje);
			}
		}
	}

	public void apagar() {
		terminado = true;
		piscina.shutdown();
		try {

			if (!servidor.isClosed()) 
				servidor.close();
			
			for (AdministradorConexiones ac : conexiones) {
				ac.apagar();
			}
		} catch(IOException e) {
			
		}
	}

	class AdministradorConexiones implements Runnable {

		private Socket cliente;
		private BufferedReader lector;
		private PrintWriter escritor;
		private String nombre;

		public AdministradorConexiones(Socket cliente) {
			this.cliente = cliente;
		}

		public void enviarMensaje(String mensaje) {
			escritor.println(mensaje);
		}
		
		public String getNombre() {
			return nombre;
		}

		public void apagar() {
			try {
				if (cliente != null) 
					cliente.close();
				
				if (lector != null) 
					lector.close();
				
				if (escritor != null) 
					escritor.close();
			} catch(IOException e) {
				
			}
		}

		@Override
		public void run() {
			try {
				escritor = new PrintWriter(cliente.getOutputStream(), true);
				lector = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
				escritor.println("Por favor inserta tu nombre: ");
				nombre = lector.readLine();
				System.out.println(nombre + " se ha conectado!");
				transmitir(nombre + " se unió al chat!");
				String mensaje;

				while ((mensaje = lector.readLine()) != null) {
					if (mensaje.equals("1")) {
						escritor.println("Digite el nombre del apartamento que quiere enviar");
						String msg=lector.readLine();
						AdministradorConexiones temp=buscarConexion(msg);
						
						if(temp!=null) 
							temp.enviarMensaje(nombre+": "+msg);
						else
							escritor.println("No existe un apartamento con este nombre");
					} 
					else if (mensaje.equals("2")){
						AdministradorConexiones ac=buscarConexion("Porteria");
						if(ac!=null)
							ac.enviarMensaje("Hay una emergencia en el apartamento: "+nombre);
						else
							escritor.println("No existe una conexion con Porteria");
						

					}
					else if(mensaje.startsWith("Porteria")) {
						String[] msg = mensaje.split(":");
						AdministradorConexiones ac=buscarConexion(msg[1]);
						if(ac!=null) {
							ac.enviarMensaje("Porteria: "+"¿Desea dejar ingresar a "+msg[2]+" Si/No");
							String mens=ac.lector.readLine();
							while(!mens.equalsIgnoreCase("Si") && !mens.equalsIgnoreCase("No")) {
								ac.enviarMensaje("Esta respuesta no está entre las opciones");
								ac.enviarMensaje("Porteria: "+"¿Desea dejar ingresar a "+msg[2]+" Si/No");
								mens=ac.lector.readLine();
							}
							escritor.println(mens);
						}
						else
							escritor.println("No existe un apartamento con este nombre");
					}
					else if (mensaje.startsWith("/quit")) {
						transmitir(nombre + " dejo el chat.");
						apagar();
					} else {
						transmitir(nombre + ": " + mensaje);
					}
				}
			} catch (IOException e) {
				apagar();
			}
		}
		
		public AdministradorConexiones buscarConexion(String nom) {
			for (AdministradorConexiones ac : conexiones) {
				if(ac.getNombre().equalsIgnoreCase(nom)) {
					return ac;
				}
			}
			
			return null;
		}
	}
	
	public static void main(String[] args) {
		Servidor servidor = new Servidor();
		servidor.run();
	}

}
