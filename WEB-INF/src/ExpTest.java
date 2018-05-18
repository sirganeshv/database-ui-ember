public class ExpTest {
	public static void main(String[] args) {
		Export exp = new Export();
		String str = "[{\"eventID\":\"4879\",\"eventProvider\":\"MSDTC Client 2\"},{\"eventID\":\"4879\",\"eventProvider\":\"MSDTC Client 2\"},{\"eventID\":\"2484\",\"eventProvider\":\"Microsoft-Windows-Immersive-Shell\",\"eventType\":\"Error\",\"timestamp\":\"03-28-2018 19:32:37\"},{\"eventID\":\"2484\",\"eventProvider\":\"Microsoft-Windows-Immersive-Shell\",\"eventType\":\"Error\",\"timestamp\":\"02-28-2018 21:39:39\"},{\"eventID\":\"2484\",\"eventProvider\":\"Microsoft-Windows-Immersive-Shell\",\"eventType\":\"Error\",\"timestamp\":\"03-28-2018 19:32:35\"}]";
		exp.exportToPdf(str);
	}
}