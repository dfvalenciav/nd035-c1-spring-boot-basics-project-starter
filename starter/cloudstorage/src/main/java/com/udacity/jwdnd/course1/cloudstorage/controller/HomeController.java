package com.udacity.jwdnd.course1.cloudstorage.controller;

import com.udacity.jwdnd.course1.cloudstorage.model.Credential;
import com.udacity.jwdnd.course1.cloudstorage.model.Note;
import com.udacity.jwdnd.course1.cloudstorage.model.User;
import com.udacity.jwdnd.course1.cloudstorage.model.UserFile;
import com.udacity.jwdnd.course1.cloudstorage.services.CredentialService;
import com.udacity.jwdnd.course1.cloudstorage.services.FileStorageService;
import com.udacity.jwdnd.course1.cloudstorage.services.NoteService;
import com.udacity.jwdnd.course1.cloudstorage.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;

@Controller
public class HomeController {
    private UserService userService;
    private NoteService noteService;
    private CredentialService credentialService;
    private FileStorageService fileStorageService;

    public HomeController(UserService userService, NoteService noteService, CredentialService credentialService, FileStorageService fileStorageService) {
        this.userService = userService;
        this.noteService = noteService;
        this.credentialService = credentialService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping ("/home")
    public String getHome (@ModelAttribute ("noteObject") Note note, @ModelAttribute("credentialObject") Credential credential,
                           Model model, Authentication authenticateAction) {
        String username = authenticateAction.getName();
        User user = this.userService.getUser(username);
        Integer userId = user.getUserid();

        model.addAttribute("userNotes", this.noteService.displayAllNotes(userId));
        model.addAttribute("unencryptedPasswordMap", this.credentialService.getUnencryptedPassword(userId));
        model.addAttribute("userCredentials", this.credentialService.displayAllCredentials(userId));
        model.addAttribute("files", this.fileStorageService.loadAllFiles(userId));
        return "home";
    }

    @PostMapping("/home/note")
    public String addNote (@ModelAttribute ("noteObject") Note note, @ModelAttribute("credentialObject") Credential credential,
                           Model model, Authentication authenticateAction) {

        String username = authenticateAction.getName();
        User user = this.userService.getUser(username);
        Integer userId = user.getUserid();
        note.setUserid(userId);

        String taskError = null;
        if (note.getNoteid() == null) {
            try {
                this.noteService.addNote(note, username);
            } catch (Exception e) {
                taskError = "Note can not be added" + e.getMessage();
            }
        } else {
            try {
                this.noteService.updateNote(note, username);
            } catch (Exception e) {
                taskError = "Note can not be updated" + e.getMessage();
            }
        }

        if(taskError == null){
            model.addAttribute("successMessage", true);
            model.addAttribute("failureMessage", false);
        }else{
            model.addAttribute("failureMessage", taskError);
        }

        return "result";
    }

    @GetMapping ("/home/deleteNote/{id}")
    public String deleteNote (@PathVariable("id") Integer id, @ModelAttribute("noteObject") Note note, Model model, Authentication authentication ){
        String taskError = null;

        try {
            this.noteService.deleteNote(id);
        }catch (Exception e) {
            taskError = "Note can not be deleted ";
        }

        if(taskError == null){
            model.addAttribute("successMessage", true);
            model.addAttribute("failureMessage", false);
        }else{
            model.addAttribute("failureMessage", taskError);
        }
        return "result";
    }

    @PostMapping("/home/credential")
    public String addCredential (@ModelAttribute("credentialObject") Credential credential, @ModelAttribute("noteObject") Note note,
                                 Model model, Authentication authentication){
        String taskError = null;

        try {
            new URL(credential.getUrl()).toURI();
        } catch (Exception e) {
            taskError = "Invalid URL";
        }

        if(taskError == null) {
            String username = authentication.getName();
            if(credential.getCredentialid() == null){
                this.credentialService.addCredential(credential, username);
            }else{
                this.credentialService.updateCredential(credential, username);
            }
            model.addAttribute("successMessage", true);
            model.addAttribute("failureMessage", false);
        }else{
            model.addAttribute("failureMessage", taskError);
        }
        return "result";
    }

    @GetMapping("/home/deleteCredential/{id}")
    public String deleteCredential(@PathVariable("id") Integer id, @ModelAttribute("credentialObject") Credential credential ,
                                   @ModelAttribute("noteObject") Note note, Model model, Authentication authentication) {

        String taskError = null;
        try {
            this.credentialService.deleteCredential(id);
        }
        catch (Exception e) {
            taskError = "Error: Selected Url Credential cannot be deleted";
        }

        if(taskError == null){
            model.addAttribute("successMessage", true);
            model.addAttribute("failureMessage", false);
        }else{
            model.addAttribute("failureMessage", taskError);
        }

        return "result";
    }

    @PostMapping("/home/file-upload")
    public String handleFileUpload (@PathVariable ("fileUpload")MultipartFile fileUpload, Model model, Authentication authentication ) throws IOException {
        String username = authentication.getName();
        User user = this.userService.getUser(username);
        Integer userId = user.getUserid();

        String taskError = null;
        if (fileUpload.isEmpty()){
            taskError = "Unable to save an empty file";
        }
        if (taskError == null){
            String fileName = fileUpload.getOriginalFilename();
            boolean fileNameInUseByUser = this.fileStorageService.isFileNameInUseByUser(userId, fileName);
            if (fileNameInUseByUser){
                taskError = "You have already stored a file under similar name";
            }
        }
        if(taskError == null){
            this.fileStorageService.storeFile(fileUpload, username);
            model.addAttribute("successMessage", true);
            model.addAttribute("failureMessage", false);
        }else{
            model.addAttribute("failureMessage", taskError);
        }

        return "result";
    }

    @GetMapping ("/home/download-file/{id}")
    public void donwloadFile (@PathVariable ("id") Integer id, HttpServletResponse httpServletResponse) throws IOException {
        UserFile userFile = this.fileStorageService.loadSingleFile(id);
        httpServletResponse.setContentType(userFile.getContenttype());
        httpServletResponse.setContentLength(Integer.valueOf(userFile.getFilesize()));
        httpServletResponse.setHeader("\"Content-Disposition", "attachment; filename=\"" + userFile.getFilename() +"\"");

        FileCopyUtils.copy(userFile.getFiledata(), httpServletResponse.getOutputStream() );
    }

    @GetMapping ("/home/deleteFile/{id}")
    public String deleteFile (@PathVariable ("id") Integer id, @ModelAttribute("credentialObject") Credential credential, @ModelAttribute("noteObject") Note note,
                              Model model, Authentication authentication) {

        String taskError = null;
        try {
            this.fileStorageService.deleteFile(id);
        } catch (Exception e){
            taskError = "Unable to delete file, try later";
        }

        if(taskError == null){
            model.addAttribute("successMessage", true);
            model.addAttribute("failureMessage", false);
        }else{
            model.addAttribute("failureMessage", taskError);
        }
        return "result";
    }
}
