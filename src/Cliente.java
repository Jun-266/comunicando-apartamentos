import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * <h1>Apartamento</h1> La aplicacion del Cliente muestra la interfaz de usuario
 * de los apartamentos y gestiona el cliente del programa
 * <p>
 * <b>Note:</b> La aplicacion fuciona por consola
 *
 * @author JOS
 * @version 1.0
 */
public class Cliente implements Runnable {

	private Socket cliente;
	private BufferedReader lector;
	private PrintWriter escritor;
	private boolean terminado;
	private String contacto;
	private String email;
	private String pass;
	private String name;

	/**
	 * Constructor para la clase Cliente
	 */
	public Cliente() {
		this.terminado = false;
		contacto = "";
		email = "";
		pass = "";
		name = "";
	}

	/**
	 * Este metodo cierra la conexion entre el cliente y el servidor y se ejecuta si
	 * el cliente digita el comando quit
	 */
	public void apagar() {
		terminado = true;
		try {
			if (lector != null)
				lector.close();

			if (escritor != null)
				escritor.close();

			if (!cliente.isClosed())
				cliente.close();
		} catch (IOException e) {

		}
	}

	/**
	 * Este metodo muestra la interfaz de usuario y lee por consola los comandos de
	 * la aplicacion de los apartamentos
	 */
	@Override
	public void run() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			System.out.println(
					"Recuerda:\n1)Comunicarte con un apartamento\n2)Boton de emergencia\nSi quieres transmitir a todos los apartamentos"
							+ " solo escribe");
			System.out.println("Digite un correo como contacto de emergencia");
			contacto = in.readLine();
			System.out.println("Digite su nombre");
			name = in.readLine();
			System.out.println("Digite su correo de gmail");
			email = in.readLine();
			System.out.println("Digite contraseña");
			pass = in.readLine();

			InetAddress ia = InetAddress.getLocalHost();
			cliente = new Socket(ia.getHostAddress(), 9999);
			escritor = new PrintWriter(cliente.getOutputStream(), true);
			lector = new BufferedReader(new InputStreamReader(cliente.getInputStream()));

			AdministradorEntradaDatos aed = new AdministradorEntradaDatos();
			Thread t = new Thread(aed);
			t.start();

			String mensaje;
			while ((mensaje = lector.readLine()) != null) {
				System.out.println(mensaje);
			}
		} catch (IOException e) {
			apagar();
		}
	}

	class AdministradorEntradaDatos implements Runnable {
		/**
		 * Este metodo lee las determinadas opciones por la linea de comandos y llama a
		 * los metodos correspondientes
		 */
		@Override
		public void run() {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
				while (!terminado) {
					String mensaje = in.readLine();
					if (mensaje.equals("/quit")) {
						escritor.println(mensaje);
						in.close();
						apagar();
					} else if (mensaje.equals("2")) {
						escritor.println(mensaje);
						enviarCorreo();
					} else {
						escritor.println(mensaje);
					}
				}
			} catch (IOException e) {
				apagar();
			}
		}

		/**
		 * Este metodo se encarga de enviar los correos de emergencia por medio del
		 * puerto 587
		 */
		public void enviarCorreo() {
			Properties properties = new Properties();
			properties.put("mail.smtp.host", "smtp.office365.com");
			properties.put("mail.smtp.starttls.enable", "true");
			properties.put("mail.smtp.port", 587);
			properties.put("mail.smtp.mail.sender", email);
			properties.put("mail.smtp.user", email);
			properties.put("mail.smtp.auth", "true");

			Session sesion = Session.getDefaultInstance(properties);
			String asunto = "Emergencia apartamento";
			String mensaje = "El residente " + name + " se encuentra en un estado emergencia."
					+ "Por favor contactarse con la persona lo antes posible";

			try {
				MimeMessage message = new MimeMessage(sesion);
				message.setFrom(new InternetAddress(email));
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(contacto));
				message.setSubject(asunto);
				message.setText(mensaje);
				Transport t = sesion.getTransport("smtp");
				t.connect(email, pass);
				t.sendMessage(message, message.getAllRecipients());
				t.close();

			} catch (MessagingException me) {
				me.printStackTrace();
			}
		}
	}

	/**
	 * Este metodo main crea un objeto cliente y ejecuta su metodo run
	 * 
	 * @param args Unused.
	 */
	public static void main(String[] args) {
		Cliente c = new Cliente();
		c.run();
	}

}
