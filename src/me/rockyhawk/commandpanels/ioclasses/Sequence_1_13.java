package me.rockyhawk.commandpanels.ioclasses;

//1.13 Imports
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CharSequenceReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public class Sequence_1_13 {
    public Reader getReaderFromStream(InputStream initialStream) throws IOException {
        //this reads the encrypted resource files in the jar file
        byte[] buffer = IOUtils.toByteArray(initialStream);
        return new CharSequenceReader(new String(buffer));
    }
}
