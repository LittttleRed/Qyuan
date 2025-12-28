package org.example.qyuanuser.service;

import org.example.qyuancommon.Result;
import org.example.qyuanuser.Result.*;
import org.example.qyuanuser.DTO.favorite.*;
import org.example.qyuanuser.entity.FavoriteFolder;

public interface UserFavoriteService {
    
    CommonResult addPaper(PaperDTO PaperDTO, int user_id);

    CommonResult deletePaper(PaperDTO PaperDTO, int user_id);

    Result<FavoriteFolder> addFolder(FolderDTO folderDTO, int user_id);

    CommonResult deleteFolder(Integer folder_id);

    FolderSpecInfoResult getFolderSpecInfo(int user_id);

    RecordSepcInfoResult getRecordSpecInfo(Integer folder_id, int user_id);

    CommonResult renameFolder(Integer folder_id, String folder_name, int user_id);

    CommonResult changeFolderState(Integer folder_id, Integer is_public, int user_id);
}
