package org.example.qyuanuser.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.qyuancommon.Result;
import org.springframework.stereotype.Service;
import org.example.qyuanuser.service.UserFavoriteService;
import org.example.qyuanuser.mapper.FavoriteRecordMapper;
import org.example.qyuanuser.mapper.FavoriteFolderMapper;

import java.util.List;
import java.util.ArrayList;

import org.example.qyuanuser.DTO.favorite.*;
import org.example.qyuanuser.entity.FavoriteFolder;
import org.example.qyuanuser.entity.FavoriteRecord;
import org.example.qyuanuser.Result.*;

@Service
public class UserFavoriteServiceImpl extends ServiceImpl<FavoriteRecordMapper, FavoriteRecord> implements UserFavoriteService {

    @Resource
    private FavoriteRecordMapper favoriteRecordMapper;
    @Resource
    private FavoriteFolderMapper favoriteFolderMapper;
    
    @Override
    public CommonResult addPaper(PaperDTO PaperDTO, int user_id){
        CommonResult result = new CommonResult();
        if(!PaperDTO.isFull()){
            result.setMessage("缺少参数");
            result.setSuccess(false);
            return result;
        }

        //插入favoriteRecord
        FavoriteRecord favoriteRecord = new FavoriteRecord();
        favoriteRecord.setFolderId(PaperDTO.getFolder_id());
        favoriteRecord.setPaperId(PaperDTO.getPaper_id());
        favoriteRecord.setPaperTitle(PaperDTO.getPaper_title());
        favoriteRecord.setUserId(user_id);
        favoriteRecordMapper.insert(favoriteRecord);

        //folder的paper_count +1
        FavoriteFolder favoriteFolder = favoriteFolderMapper.selectById(PaperDTO.getFolder_id());
        favoriteFolder.setPaperCount(favoriteFolder.getPaperCount() + 1);
        favoriteFolderMapper.updateById(favoriteFolder);

        //返回
        result.setSuccess(true);
        return result;
    }

    @Override
    public CommonResult deletePaper(PaperDTO PaperDTO, int user_id){ 
        CommonResult result = new CommonResult();
        if(!PaperDTO.isFull()){
            result.setMessage("缺少参数");
            result.setSuccess(false);
            return result;
        }

        //删除favoriteRecord
        QueryWrapper<FavoriteRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("paper_id", PaperDTO.getPaper_id());
        wrapper.eq("user_id", user_id);
        wrapper.eq("folder_id", PaperDTO.getFolder_id());
        int deleteCount = favoriteRecordMapper.delete(wrapper);

        if (deleteCount > 0) {
            // folder的paper_count -1
            FavoriteFolder favoriteFolder = favoriteFolderMapper.selectById(PaperDTO.getFolder_id());
            if (favoriteFolder != null && favoriteFolder.getPaperCount() > 0) {
                favoriteFolder.setPaperCount(favoriteFolder.getPaperCount() - 1);
                favoriteFolderMapper.updateById(favoriteFolder);
            }
            result.setSuccess(true);
            result.setMessage("删除成功");
        } else {
            result.setSuccess(false);
            result.setMessage("未找到对应的收藏记录或删除失败");
        }

        return result;
    }

    @Override
    public Result<FavoriteFolder> addFolder(FolderDTO folderDTO, int user_id){
        Result<FavoriteFolder> result = new Result();
        if(!folderDTO.isFull()){
            result.setMsg("缺少参数");
            result.setCode(500);
            return result;
        }

        FavoriteFolder favoriteFolder = new FavoriteFolder();
        favoriteFolder.setFolderName(folderDTO.getFolder_name());
        favoriteFolder.setUserId(user_id);
        favoriteFolder.setIsPublic(folderDTO.getIs_public());
        favoriteFolder.setPaperCount(0);
        favoriteFolderMapper.insert(favoriteFolder);
        return Result.ok(favoriteFolder);
    }

    @Override
    public CommonResult deleteFolder(Integer folderId){ 
        CommonResult result = new CommonResult();
        if (folderId == null) {
            result.setMessage("缺少参数");
            result.setSuccess(false);
            return result;
        }
        favoriteFolderMapper.deleteById(folderId);
        result.setSuccess(true);
        return result;
    }

    @Override
    public FolderSpecInfoResult getFolderSpecInfo(int user_id){ 
        // 创建查询条件，根据user_id查询该用户的所有文件夹

        List<FavoriteFolder> folderList = favoriteFolderMapper.selectByUserId(user_id);
        
        // 将FavoriteFolder实体列表转换为FolderSpecInfo对象列表
        ArrayList<FolderSpecInfoResult.FolderSpecInfo> folderSpecInfoes = new ArrayList<>();
        for (FavoriteFolder folder : folderList) {
            FolderSpecInfoResult.FolderSpecInfo info = new FolderSpecInfoResult.FolderSpecInfo();
            info.setFolder_id(folder.getFolderId());
            info.setUser_id(folder.getUserId());
            info.setFolder_name(folder.getFolderName());
            info.setIs_public(folder.getIsPublic());
            info.setPaper_count(folder.getPaperCount());
            folderSpecInfoes.add(info);
        }
        
        // 构建结果对象
        FolderSpecInfoResult result = new FolderSpecInfoResult();
        result.setSuccess(true);
        result.setData(folderSpecInfoes);
        return result;
    }

    @Override
    public RecordSepcInfoResult getRecordSpecInfo(Integer folder_id, int user_id){ 
        RecordSepcInfoResult result = new RecordSepcInfoResult();
        if (folder_id == null) {
            result.setMessage("缺少参数");
            result.setSuccess(false);
            return result;
        }
        QueryWrapper<FavoriteRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("folder_id", folder_id);
        wrapper.eq("user_id", user_id);
        List<FavoriteRecord> recordList = favoriteRecordMapper.selectList(wrapper);

        ArrayList<RecordSepcInfoResult.RecordSepcInfo> recordSepcInfoes = new ArrayList<>();
        for (FavoriteRecord record : recordList) {
            RecordSepcInfoResult.RecordSepcInfo info = new RecordSepcInfoResult.RecordSepcInfo();
            info.setRecord_id(record.getRecordId());
            info.setFolder_id(record.getFolderId());
            info.setPaper_id(record.getPaperId());
            //TODO : 获取paper_title
            info.setPaper_title(record.getPaperTitle());
            info.setUser_id(record.getUserId());
            recordSepcInfoes.add(info);
        }
        result.setSuccess(true);
        result.setData(recordSepcInfoes);
        return result;
    }


    @Override
    public CommonResult renameFolder(Integer folder_id, String folder_name, int user_id){ 
        CommonResult result = new CommonResult();
        if(folder_id == null ||folder_name == null){
            result.setMessage("缺少参数");
            result.setSuccess(false);
            return result;
        }
        FavoriteFolder favoriteFolder = favoriteFolderMapper.selectById(folder_id);
        if(favoriteFolder == null){
            result.setMessage("未找到对应的文件夹");
            result.setSuccess(false);
            return result;
        }

        favoriteFolder.setFolderName(folder_name);
        favoriteFolderMapper.updateById(favoriteFolder);
        result.setSuccess(true);
        return result;
    }

    @Override
    public CommonResult changeFolderState(Integer folder_id, Integer is_public, int user_id){ 
        CommonResult result = new CommonResult();
        if(folder_id == null || is_public == null){
            result.setMessage("缺少参数");
            result.setSuccess(false);
            return result;
        }
        FavoriteFolder favoriteFolder = favoriteFolderMapper.selectById(folder_id);
        if(favoriteFolder == null){
            result.setMessage("未找到对应的文件夹");
            result.setSuccess(false);
            return result;
        }
        favoriteFolder.setIsPublic(is_public);
        favoriteFolderMapper.updateById(favoriteFolder);
        result.setSuccess(true);
        return result;
    }
}