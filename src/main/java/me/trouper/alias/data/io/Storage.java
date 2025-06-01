package me.trouper.alias.data.io;

import me.trouper.alias.utils.Verbose;
import me.trouper.alias.utils.misc.JsonSerializable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Storage implements JsonSerializable<Storage> {

    @Override
    public File getFile() {
        return me.trouper.alias.Alias.getInstance().getManager().io.STORAGE_FILE;
    }

    @Override
    public void save() {
        Verbose.send(1,"Saving Storage...");
        JsonSerializable.super.save();
    }
}
