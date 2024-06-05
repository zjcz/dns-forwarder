package dev.jonclarke.dnsforwarder.helpers;

public class ByteHelper {
    /**
     * Converts a byte array to a string representation and returns it.
     * This can then be used for debugging purposes (i.e. pasting from the console into a test)
     * @param byteArray The byte array to convert
     */
    public static String convertByteArrayToPrintStatement(byte[] byteArray) {
        // Convert the byte array to a string representation
        StringBuilder sb = new StringBuilder();
        sb.append("byte[] data = {");
        for (int i = 0; i < byteArray.length; i++) {
            sb.append(byteArray[i]);
            if (i < byteArray.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("};");

        return sb.toString();
    }
}
