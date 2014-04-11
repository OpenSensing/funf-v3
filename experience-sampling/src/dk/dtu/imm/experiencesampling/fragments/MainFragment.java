package dk.dtu.imm.experiencesampling.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.dtu.imm.experiencesampling.R;
import dk.dtu.imm.experiencesampling.db.DatabaseHelper;
import dk.dtu.imm.experiencesampling.enums.QuestionType;
import dk.dtu.imm.experiencesampling.external.FacebookService;
import dk.dtu.imm.experiencesampling.models.Friend;
import dk.dtu.imm.experiencesampling.models.answers.Answer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

// todo: not necessary when used within the sensible dtu data-collector
public class MainFragment extends Fragment {

    ProgressDialog mProgress;

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        if (rootView != null) {
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getActivity() != null) {

                        int viewId = v.getId();
                        if (viewId == R.id.main_button_facebook_explorer) {
                            String url = "https://developers.facebook.com/tools/explorer";
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(url));
                            startActivity(i);
                        } else if (viewId == R.id.main_button_facebook) {
                            EditText editAccessToken = (EditText) rootView.findViewById(R.id.main_edit_accesstoken);
                            String accessToken = (editAccessToken != null && editAccessToken.getText() != null) ? editAccessToken.getText().toString() : null;
                            if (accessToken != null && !accessToken.isEmpty()) {
                                collectFacebookFriendList(accessToken);
                            } else {
                                Toast.makeText(getActivity(), "Please provide an access token", Toast.LENGTH_SHORT).show();
                            }
                        } else if (viewId == R.id.main_button_export_answers) {
                            exportAnswers();
                        }
                    }
                }
            };

            Button facebookButton = (Button) rootView.findViewById(R.id.main_button_facebook);
            Button facebookExpButton = (Button) rootView.findViewById(R.id.main_button_facebook_explorer);
            Button answerExportButton = (Button) rootView.findViewById(R.id.main_button_export_answers);

            facebookButton.setOnClickListener(onClickListener);
            facebookExpButton.setOnClickListener(onClickListener);
            answerExportButton.setOnClickListener(onClickListener);
        }
        return rootView;
    }

    private void collectFacebookFriendList(final String accessToken) {
        startProgressDialog("Collecting friend list");
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    FacebookService facebookService = new FacebookService(accessToken);
                    List<Friend> friends = facebookService.getFriends();

                    DatabaseHelper database = new DatabaseHelper(getActivity());
                    database.insertFriends(friends);
                    makeToast("Facebook friends collected");
                } catch (Exception e) {
                    makeToast("Error during Facebook request");
                }
                stopProgressDialog();
            }
        });
        thread.start();
    }

    private void exportAnswers() {
        startProgressDialog("Exporting answers to json files");
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                boolean allSuccess = true;
                try {
                    for (QuestionType type : QuestionType.values()) {
                        String json = getAnswersAsJsonString(type);
                        boolean success = writeExternalFile(json, type.name());
                        if (!success) {
                            allSuccess = false;
                        }
                    }
                    if (allSuccess) {
                        makeToast("All answers exported successfully");
                    } else {
                        makeToast("Some answers are not exported correct");
                    }
                } catch (Exception e) {
                    makeToast("Error during answer export");
                }
                stopProgressDialog();
            }
        });
        thread.start();
    }

    private String getAnswersAsJsonString(QuestionType type) {
        String json = "";
        if (getActivity() != null) {
            ObjectMapper mapper = new ObjectMapper();
            DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
            try {
                List<Answer> answers = dbHelper.readAnswers(type);
                json = mapper.writeValueAsString(answers);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            dbHelper.closeDatabase();
        }
        return json;
    }

    private boolean writeExternalFile(String text, String filename) {
        try {
            //This will get the SD Card directory and create a folder named MyFiles in it.
            File sdCard = Environment.getExternalStorageDirectory();
            File directory = new File (sdCard.getAbsolutePath() + "/Android/data/dk.dtu.locationwidget/answers");
            directory.mkdirs();

            //Now create the file in the above directory and write the contents into it
            File file = new File(directory, filename + ".json");
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(fOut);
            osw.write(text);
            osw.flush();
            osw.close();

            if (getActivity() != null) {
                getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void makeToast(final String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void startProgressDialog(String message) {
        if (getActivity() != null) {
            mProgress = ProgressDialog.show(getActivity(), "Please wait", message, true, true);
        }
    }

    private void stopProgressDialog() {
        if (mProgress != null) {
            mProgress.dismiss();
        }
    }
}