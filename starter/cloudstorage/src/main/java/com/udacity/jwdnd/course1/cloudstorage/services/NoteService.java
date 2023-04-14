package com.udacity.jwdnd.course1.cloudstorage.services;

import com.udacity.jwdnd.course1.cloudstorage.mapper.NoteMapper;
import com.udacity.jwdnd.course1.cloudstorage.model.Note;
import com.udacity.jwdnd.course1.cloudstorage.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoteService {
    private NoteMapper noteMapper;
    private UserService userService ;

    public NoteService(NoteMapper noteMapper, UserService userService) {
        this.noteMapper = noteMapper;
        this.userService = userService;
    }

    public int addNote (Note note, String username) {
        String noteTitle = note.getNoteTitle();
        String noteDescription = note.getNoteDescription();
        Integer noteId = note.getNoteId();
        User user = userService.getUser(username);
        return noteMapper.insertNote(new Note(noteId, noteTitle, noteDescription, user.getUserId()));
    }

    public void updateNote (Note note, String username){
        String noteTitle = note.getNoteTitle();
        String noteDescription = note.getNoteDescription();
        Integer noteId = note.getNoteId();
        User user = userService.getUser(username);
         this.noteMapper.updateNote(noteId, noteTitle, noteDescription, user.getUserId());
    }

    public void deleteNote (Integer noteId) { noteMapper.deleteNote(noteId);}

    public List<Note> displayAllNotes(Integer userid){
        return noteMapper.retrieveAllNotes(userid);
    }

}
