import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Tools {

    public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream b = new ByteArrayInputStream(bytes)) {
            try (ObjectInputStream o = new ObjectInputStream(b)) {
                return o.readObject();
            }
        }
    }

    public static byte[] serialize(Object obj) throws IOException {
        try(ByteArrayOutputStream b = new ByteArrayOutputStream()){
            try(ObjectOutputStream o = new ObjectOutputStream(b)){
                o.writeObject(obj);
            }
            return b.toByteArray();
        }
    }

    public static Boolean isNullOrEmpty(Object obj) {
        if(obj == null || obj.toString().length() == 0)
            return true;
        return false;
    }

        /**
	 * Reduce the size of a string if it is longer then 256 char
	 * @param text the string to format
	 * @return the string formatted
	 */
	public static String formatStringLengthMax(String text) {

		if (text == null)
			return null;

		if (text.length() > 256)
			text = text.substring(0, 256);

		return text;
	}
}
