package sms.callback;

public class TestApp {
	public static void main(String[] args) {
		try {
			SMSReTryHandler.processCall(
					"Sent from your Twilio trial account - Sent from your Twilio trial account - <11835760294> Testing from processCall");
		} finally {
//			if (DBConnectionManager.getInstance().provisionPool != null) {
//				try {
//					DBConnectionManager.getInstance().getProvisionPool().shutdown();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
		}
	}
}