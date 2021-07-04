public class Util {
	public static boolean isInteger(String str) {
		boolean isInteger = true;
		try {
			Integer.parseInt(str);
		} catch (Exception e)
		{
			isInteger = false;
		}
		return isInteger;
	}
}
