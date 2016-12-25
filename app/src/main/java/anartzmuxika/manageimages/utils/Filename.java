package anartzmuxika.manageimages.utils;

/**********************************************************************
 * Created by anartzmugika on 25/12/16.
 */

public class Filename {

    public String getFilenameWithNameBasicData(String name_basic_data, String filename)
    {
        if (!name_basic_data.equals("")) name_basic_data = name_basic_data.replace(" ", "_").toLowerCase();
        return String.format(filename, name_basic_data);
    }
}

