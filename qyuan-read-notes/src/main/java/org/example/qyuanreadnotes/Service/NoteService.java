package org.example.qyuanreadnotes.Service;

import org.example.qyuanreadnotes.Entity.Note;

import java.util.List;

public interface NoteService {
    List<Note> getNote(int userId, Integer paperId);

    Note addNotes(int userId, Integer paperId);

    Note updateNotes(Integer noteId, String title, String content);

    void deleteNotes(Integer noteId);
}
