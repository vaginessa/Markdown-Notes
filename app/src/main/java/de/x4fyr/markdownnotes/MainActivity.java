package de.x4fyr.markdownnotes;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import de.x4fyr.markdownnotes.utils.Note;
import de.x4fyr.markdownnotes.utils.NoteAdapter;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

/**
 * Main Activity, from where folders are browsed to open notes.
 */
public class MainActivity extends AppCompatActivity {

    public File folder = Environment.getExternalStorageDirectory();

    private final Context mainContext = this;

    private EditText locationEditText;
    private final EditText.OnEditorActionListener locationEditTextListener = this::changeFolderEditorAction;

    /**
     * Change the current folder scope of the main activity.
     *
     * @param newFolder File object containing the folder.
     */
    public void changeFolder(File newFolder) {
        try {
            if (newFolder.exists() && newFolder.isDirectory()) {
                folder = newFolder;
                createCards(getNotesFromFolder(folder));
                locationEditText.setText(folder.getAbsolutePath());
            } else {
                Toast.makeText(mainContext, String.format(getString(R.string.toast_folder_does_not_exists),
                               folder.getAbsoluteFile()), Toast.LENGTH_SHORT).show();
                locationEditText.setText(folder.getAbsoluteFile().toString());
            }
        } catch (Exception exception) {
            Toast.makeText(mainContext, R.string.toast_not_possible, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean changeFolderEditorAction(TextView view, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_GO
            && (event == null || (event.getAction() == KeyEvent.ACTION_DOWN && !event.isCanceled()))) {
            File newFolder = new File(locationEditText.getText().toString().trim());
            changeFolder(newFolder);
        }
        return true;
    }

    private ArrayList<Note> getNotesFromFolder(File folder) {
        ArrayList<Note> notes = new ArrayList<>();

        /**
         * Filter for Markdown files.
         */
        class MarkdownFileFilter implements FileFilter {
            @Override
            public boolean accept(File file) {
                try {
                    //noinspection HardCodedStringLiteral
                    return file.isFile() && file.getName().substring(file.getName().lastIndexOf(".")).equals(".md");
                } catch (Exception exception) {
                    return false;
                }
            }
        }

        File[] files = folder.listFiles(new MarkdownFileFilter());
        if (files.length != 0) {
            for (File file : files) {
                notes.add(new Note(file));
            }
        } else {
            Toast.makeText(mainContext, R.string.toast_no_notes_found, Toast.LENGTH_SHORT).show();
        }

        return notes;
    }

    private void createCards(ArrayList<Note> notes) {


        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.note_card_recycler_view);

        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        RecyclerView.Adapter adapter = new NoteAdapter(notes, this);
        recyclerView.setAdapter(adapter);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String strFolder = sharedPref.getString("pref_key_startup_folder", "");


        if (!"".equals(strFolder)) {
            folder = new File(strFolder);
            if (!folder.exists() || !folder.isDirectory() || !folder.canRead()) {
                folder = Environment.getExternalStorageDirectory();
            }
        }

        //Make toolbar as actionbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        locationEditText = (EditText) findViewById(R.id.location_editText);
        locationEditText.setText(folder.getAbsolutePath());
        locationEditText.setOnEditorActionListener(locationEditTextListener);
        findViewById(R.id.add_button).setOnClickListener(this::addItem);
        findViewById(R.id.note_card_recycler_view).setOnClickListener(this::viewItem);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        createCards(getNotesFromFolder(folder));
    }

    @Override
    public void onBackPressed() {
        if (folder.getParentFile() == null) {
            super.onBackPressed();
        } else {
            changeFolder(folder.getParentFile());
        }
    }

    /**
     * Trigger for view actions to add a new note item.
     *
     * @param view Not used but needed for the OnClickListener interface.
     */
    public void addItem(View view) {
        Intent intent = new Intent(this, EditorActivity.class);
        //noinspection HardCodedStringLiteral
        intent.putExtra("de.x4fyr.markdown_notes.CURRENT_NOTE", folder);
        startActivity(intent);
    }

    /**
     * Trigger for view actions to add a new note item.
     *
     * <p>Launches the EditorActivity.</p>
     *
     * @param view Used to determine the filename of the item.
     *             This is got from a TextView with the id note_card_filename in this view.
     */
    public void viewItem(View view) {
        Intent intent = new Intent(this, EditorActivity.class);
        String filename = ((TextView) view.findViewById(R.id.note_card_filename)).getText().toString().trim();
        File file = new File(folder.getName() + "/" + filename);
        //noinspection HardCodedStringLiteral
        intent.putExtra("de.x4fyr.markdown_notes.CURRENT_NOTE", file);
        TextView wvTitle = ((TextView) view.findViewById(R.id.note_card_content));
        //noinspection HardCodedStringLiteral
        ActivityOptions options  = ActivityOptions.makeSceneTransitionAnimation(this, wvTitle, "rendered_view");
        startActivity(intent, options.toBundle());
    }

}
