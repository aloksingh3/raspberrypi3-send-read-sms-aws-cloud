package sms.callback;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handles the webhook configured at Twilio to forward the SMS to AerFrame
 *
 */
public class WebHookHandler extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final String AERFRAME_API_URL = "<Api url>";
	private static final String API_KEY = "<api key>";
	private static final String ACC_NUMBER = "<account no>";
	private static final String SHORT_APP_NAME = "pidayapp";

//	private static final String QUERY = "SELECT IMSI FROM AERBILL_PROV.DEVICE WHERE MSISDN = ?";

	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		final String twilioSMSBody = req.getParameter("Body");
		System.out.println("Twilio sent SMS body as " + twilioSMSBody);
		processCall(twilioSMSBody);
		sendEmptyTwimlResponse(resp);
	}

	@Override
	public void destroy() {
//		if (DBConnectionManager.getInstance().provisionPool != null) {
//			try {
//				DBConnectionManager.getInstance().getProvisionPool().shutdown();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
	}

	/**
	 * Processes the call as received from Twilio.
	 * 
	 * @param smsBody
	 */
	public static void processCall(final String smsBody) {
		if (smsBody == null || smsBody.trim().isEmpty()) {
			System.out.println("SMS Body received empty");
			throw new RuntimeException(
					"SMS Body not correctly formatted, the format should be <{PhoneNumber}> {SMS Content}");
		}

		if (smsBody.indexOf("<") == -1 || smsBody.indexOf(">") == -1) {
			System.out.println("SMS Body received " + smsBody);
			throw new RuntimeException(
					"SMS Body not correctly formatted, the format should be <{PhoneNumber}> {SMS Content}");
		}

		final String toNumber = getToNumber(smsBody);
		final String smsContent = getSMSContent(smsBody);
		sendMsgToAerFrame(ACC_NUMBER, SHORT_APP_NAME, toNumber, smsContent);
	}

	/**
	 * Returns the Aeris "To" Number from the SMS Body
	 * 
	 * @param smsBody
	 * @return
	 */
	private static String getToNumber(final String smsBody) {
		return smsBody.substring(smsBody.indexOf("<") + 1, smsBody.indexOf(">"));
	}

	/**
	 * Returns the actual content to be sent in the SMS
	 * 
	 * @param smsBody
	 * @return
	 */
	private static String getSMSContent(final String smsBody) {
		return smsBody.substring(smsBody.indexOf(">") + 2, smsBody.length());
	}

	/**
	 * Sends the sms message to the AerFrame API to deliver this to the Aeris
	 * SIM
	 * 
	 * @param accountNumber
	 * @param appName
	 * @param toNumber
	 * @param smsContent
	 */
	private static void sendMsgToAerFrame(final String accountNumber, final String appName, final String toNumber,
			final String smsContent) {
		HttpURLConnection conn = null;
		try {
			final String imsi = getIMSIFromNumber(toNumber);
			if (imsi == null) {
				throw new RuntimeException("IMSI not found for the given MSISDN " + toNumber);
			}

			final URL url = new URL(
					AERFRAME_API_URL + accountNumber + "/outbound/" + appName + "/requests?apiKey=" + API_KEY);
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Accept", "application/json");

			String input = "{\"address\":[\"" + imsi
					+ "\"],\"senderAddress\":\"piaerframe\",\"outboundSMSTextMessage\":{\"message\":\"" + smsContent
					+ "\",\"clientCorrelator\":\"1234\",\"senderName\":\"PiTestClient\"}}";
			System.out.println("Aerframe API request payload " + input);

			OutputStream os = conn.getOutputStream();
			os.write(input.getBytes());
			os.flush();

			System.out.println("Response Code " + conn.getResponseCode());
			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
			String output;
			System.out.println("Output from Server .... \n");

			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}
		} catch (Exception e) {
			throw new RuntimeException("Exception occurred while calling AerFrame API to send message", e);
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

	/**
	 * Returns the IMSI from the MSISDN sent in the message body.
	 * 
	 * @param toNumber
	 * @return
	 */
	private static String getIMSIFromNumber(final String toNumber) {
		return "<imsi no>";
	}

	/**
	 * Returns an empty response to Twilio once the message is sent to AerFrame
	 * 
	 * @param resp
	 * @throws IOException
	 */
	private static void sendEmptyTwimlResponse(HttpServletResponse resp) throws IOException {
		resp.setContentType("application/xml");
		PrintWriter out = resp.getWriter();
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<Response>");
		sb.append("</Response>");
		out.println(sb.toString());
		out.flush();
	}
}