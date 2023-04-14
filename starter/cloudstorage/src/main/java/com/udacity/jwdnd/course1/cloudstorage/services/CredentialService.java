package com.udacity.jwdnd.course1.cloudstorage.services;

import com.udacity.jwdnd.course1.cloudstorage.mapper.CredentialMapper;
import com.udacity.jwdnd.course1.cloudstorage.model.Credential;
import com.udacity.jwdnd.course1.cloudstorage.model.User;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CredentialService {

    private CredentialMapper credentialMapper;
    private EncryptionService encryptionService;
    private UserService userService;

    public CredentialService(CredentialMapper credentialMapper, EncryptionService encryptionService, UserService userService) {
        this.credentialMapper = credentialMapper;
        this.encryptionService = encryptionService;
        this.userService = userService;
    }

    public int addCredential (Credential credential, String username) {
        User user = this.userService.getUser(username);
        Integer userid = user.getUserId();
        SecureRandom secureRandom = new SecureRandom();
        byte [] bytes = new byte[16];
        secureRandom.nextBytes(bytes);
        String encodedKey = Base64.getEncoder().encodeToString(bytes);
        String encryptedPassword = this.encryptionService.encryptValue(credential.getPassword(), encodedKey);
        return this.credentialMapper.insertCredential(new Credential(null, credential.getUrl(), credential.getUsername(),
                encodedKey, encryptedPassword, userid.intValue()));
    }

    public void updateCredential (Credential credential, String username){
        User user = this.userService.getUser(username);
        Integer userid = user.getUserId();
        Integer credentialId = credential.getCredentialid();
        String credentialUrl = credential.getUrl();
        String credentialUsername = credential.getUsername();
        SecureRandom secureRandom = new SecureRandom();
        byte [] bytes = new byte[16];
        secureRandom.nextBytes(bytes);
        String encodedKey = Base64.getEncoder().encodeToString(bytes);
        String encryptedPassword = this.encryptionService.encryptValue(credential.getPassword(), encodedKey);
        this.credentialMapper.updateCredential(credentialId, credentialUrl, credentialUsername, encodedKey, encryptedPassword, userid.intValue());
    }

    public void deleteCredential (Integer credentialId) {this.credentialMapper.deleteCredential(credentialId);}

    public List<Credential> displayAllCredentials(Integer userid){
        return credentialMapper.retrieveAllCredentials(userid);
    }

    public Map<Integer, String> getUnencryptedPassword (Integer userId){
        Map<Integer, String> passwordMap = new HashMap<>();
        List<Credential> listCredentials = this.credentialMapper.retrieveAllCredentials(userId);
        for (Credential credential : listCredentials) {
            passwordMap.put(credential.getCredentialid(), this.encryptionService.encryptValue(credential.getPassword(), credential.getKey()));
        }
        return passwordMap;
    }

}
