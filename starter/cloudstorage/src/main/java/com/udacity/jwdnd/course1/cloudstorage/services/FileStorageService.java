package com.udacity.jwdnd.course1.cloudstorage.services;

import com.udacity.jwdnd.course1.cloudstorage.mapper.FileMapper;
import com.udacity.jwdnd.course1.cloudstorage.model.User;
import com.udacity.jwdnd.course1.cloudstorage.model.UserFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public class FileStorageService {
    private FileMapper fileMapper;
    private UserService userService;

    public FileStorageService(FileMapper fileMapper, UserService userService) {
        this.fileMapper = fileMapper;
        this.userService = userService;
    }

    /**
     * Store file to specific user
     * */
    public  int storeFile (MultipartFile multipartFile, String username) throws IOException {
        User user = userService.getUser(username);
        Integer userId = user.getUserId();
        return fileMapper.insertUserFile(new UserFile(null, multipartFile.getOriginalFilename(),multipartFile.getContentType(),
                Long.toString(multipartFile.getSize()), userId.intValue(), multipartFile.getInputStream()));
    }

    public List<UserFile> loadAllFiles (Integer userId){
        return fileMapper.retrieveAllUserFiles(userId);
    }

    public UserFile loadSingleFile (Integer fileId) { return fileMapper.getUserFile(fileId);}

    public void deleteFile (Integer fileId) { fileMapper.deleteUserFile(fileId);}

    public boolean isFileNameInUseByUser(Integer userid, String fileName){

        List<UserFile> userFiles = fileMapper.retrieveAllUserFiles(userid);
        for(UserFile file: userFiles){
            if(file.getFilename().equals(fileName)){
                return true;
            }
        }
        return false;
    }
}
