package me.rockyhawk.commandpanels.ioclasses;

//1.14-1.17 Imports
import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.IOUtils;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.input.CharSequenceReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public class Sequence_1_14 {
    CommandPanels plugin;
    public Sequence_1_14(CommandPanels pl) {
        this.plugin = pl;
    }

    public Reader getReaderFromStream(InputStream initialStream) throws IOException {
        //this reads the encrypted resource files in the jar file
        byte[] buffer = IOUtils.toByteArray(initialStream);
        return new CharSequenceReader(new String(buffer));
    }
}
