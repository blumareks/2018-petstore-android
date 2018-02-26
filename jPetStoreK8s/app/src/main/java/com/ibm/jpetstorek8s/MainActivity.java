package com.ibm.jpetstorek8s;

import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ibm.watson.developer_cloud.android.library.audio.StreamPlayer;
import com.ibm.watson.developer_cloud.android.library.camera.CameraHelper;
import com.ibm.watson.developer_cloud.android.library.camera.GalleryHelper;
import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.Voice;
import com.ibm.watson.developer_cloud.visual_recognition.v3.VisualRecognition;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifyImagesOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.DetectedFaces;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.Face;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualClassification;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualRecognitionOptions;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    Button button;
    ImageView loadedImage;
    File image;

    //watson developer cloud java & android helpers
    private StreamPlayer streamPlayer;
    private CameraHelper cameraHelper;
    private GalleryHelper galleryHelper;
    private VisualRecognition visualService;
    private TextToSpeech textToSpeechService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Watson
        textView = (TextView) findViewById(R.id.textView);
        button = (Button) findViewById(R.id.button);
        loadedImage = (ImageView) findViewById(R.id.loaded_image);
        cameraHelper = new CameraHelper(this);

        visualService = initVisualRecognitionService();
        textToSpeechService = initTextToSpeechService();

        //fire action when button is pressed
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Logging to the console that the button pressed");
                System.out.println("Logging camera helper");
                cameraHelper.dispatchTakePictureIntent();


            }
        });
    }

    private class WatsonTask extends AsyncTask<Integer, Void, String> {
        @Override
        protected String doInBackground(Integer... integers) {

            //String ttsResponse = "This is Watson-Cloudinary Smart Selfie. Your picture looks great.";
            String ttsResponse = "This is Watson- Kube er net is J Pet Store in IBM Cloud. Your picture looks great. ";
            String textMessage = "";
            String keyword = "";

            if (image != null) {
                ClassifyImagesOptions options = new ClassifyImagesOptions.Builder()
                        .images(image)
                        .threshold(.5)
                        .build();

                VisualClassification result1 = visualService.classify(options).execute();
                System.out.println(result1);

                //check if it is a tiger - if yes say so
                boolean isEndangeredSpecies = false;
                String animal = "dog";
                boolean isAnimal = false;

                if (result1 != null && !result1.toString().equalsIgnoreCase("{}")) {
                    System.out.println("Visual Classification obj not null");

                    if (!result1.getImages().get(0).getClassifiers().isEmpty()) {
                        ttsResponse = ttsResponse + " I see potentially the following classification : ";

                        for (int i = 0; i < result1.getImages().get(0).getClassifiers().get(0).getClasses().size(); i++) {

                            if (i == 0) {
                                keyword = result1.getImages().get(0).getClassifiers().get(0).getClasses().get(0).getName();
                                animal = keyword +
                                        " with a score " + result1.getImages().get(0).getClassifiers().get(0).getClasses().get(i).getScore();

                            }

                            if ((result1.getImages().get(0).getClassifiers().get(0).getClasses().get(i).getName()).contains("tiger")) {
                                animal = result1.getImages().get(0).getClassifiers().get(0).getClasses().get(i).getName() +
                                        " with a score " + result1.getImages().get(0).getClassifiers().get(0).getClasses().get(i).getScore();
                                ;
                                isEndangeredSpecies = true;
                            }
                            if ((result1.getImages().get(0).getClassifiers().get(0).getClasses().get(i).getName()).contains("animal")) {
                                isAnimal = true;
                            }
                            String nameClassifier = result1.getImages().get(0).getClassifiers().get(0).getClasses().get(i).getName() +
                                    " with a score " + result1.getImages().get(0).getClassifiers().get(0).getClasses().get(i).getScore();
                            System.out.println("found class: " + nameClassifier);
                            //ttsResponse = ttsResponse + nameClassifier + ", ";
                        }

                        if (isEndangeredSpecies) {
                            ttsResponse = ttsResponse + " It is a " + animal + " - it is the endangered species - call 999 888 333 - thank you for helping save the endagered species for the next generations";
                            textMessage = "It is a " + animal + ". Attention! It is the endangered species - call 999 888 333 - thank you for helping save the endagered species for the next generations";

                        } else if (isAnimal) {
                            // TODO: check the price in the JPetStore

                            String[] keyword_tokenized = keyword.split(" ");
                            System.out.println("first token: " + keyword_tokenized[0]);
                            double price = 0.0;
                            try {
                                String jpetstoreServer = getString(R.string.jpetstore_url);
                                String url = jpetstoreServer + "/Search?keyword="+keyword_tokenized[0];
                                URL obj = new URL(url);
                                System.out.println("Sending 'GET' request to URL : " + url);

                                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                                int responseCode = con.getResponseCode();
                                System.out.println("\nSending 'GET' request to URL : " + url);
                                System.out.println("Response Code : " + responseCode);


                                BufferedReader in = new BufferedReader(
                                        new InputStreamReader(con.getInputStream()));
                                String inputLine;
                                StringBuffer response = new StringBuffer();
                                while ((inputLine = in.readLine()) != null) {
                                    response.append(inputLine);
                                }
                                in.close();

                                //print in String
                                System.out.println(response.toString());

                                //Read JSON response and print
                                JSONObject myResponse = new JSONObject(response.toString());

                                System.out.println("result after Reading JSON Response");
                                System.out.println("found- "+myResponse.getString("found"));

                                if ("true".equalsIgnoreCase(myResponse.getString("found"))){
                                    System.out.println("price- "+myResponse.getDouble("price"));
                                    price = myResponse.getDouble("price");
                                }


                            } catch (Exception e) {
                                System.out.println("error after Reading JSON Response");

                            }
                            String priceText = ". There isn't such an animal in the J Pet Store.";
                            if (price > 0.0) {
                                priceText = ". The price is " + price + "dollars";
                            }

                            ttsResponse = ttsResponse + "it is a " + animal + priceText;
                            textMessage = "This is Watson-JPetStore running Open Liberty. It is the " + animal + priceText;
                        }
                    }
                }

                if (!isAnimal) {

                    VisualRecognitionOptions options2 = new VisualRecognitionOptions.Builder()
                            .images(image)
                            .build();

                    DetectedFaces faces = visualService.detectFaces(options2).execute();
                    String showAgeMin, showAgeMax, showGender;
                    System.out.println(faces);

                    if (faces != null && !faces.toString().equalsIgnoreCase("{}")) {
                        System.out.println("faces obj not null");
                        if (faces.getImages().get(0).getFaces() != null && !faces.getImages().get(0).getFaces().isEmpty()) {
                            Face face = faces.getImages().get(0).getFaces().get(0);
                            Face.Age age = faces.getImages().get(0).getFaces().get(0).getAge();
                            showAgeMin = Integer.toString(age.getMin());
                            //showAgeMax = Integer.toString(age.getMax());
                            showGender = face.getGender().getGender();
                            if (face.getGender().getScore() == 0.0) {
                                if ("FEMALE".equalsIgnoreCase(showGender)) {
                                    showGender = "MALE";
                                } else {
                                    showGender = "FEMALE";
                                }
                            }

                            ttsResponse = ttsResponse + ". And I see you are taking picture of a person - probably " + showGender + " who is about " + showAgeMin + " years old.";
                            textMessage = "Picture of a person - probably " + showGender + " who is about " + showAgeMin + " years old.";
                        }

                    }
                }


                //invoke text to speech service
                System.out.println("Logging invoking Watson TTS");
                System.out.println(ttsResponse);

                streamPlayer = new StreamPlayer();
                streamPlayer.playStream(textToSpeechService.synthesize(ttsResponse
                        , Voice.EN_MICHAEL).execute());

                System.out.println("------ the returned msg: " + textMessage);
                return textMessage;
            } else {
                return "There is a problem with a picture - try once more";
            }
        }

        //setting the value of UI outside of the thread
        @Override
        protected void onPostExecute(String result) {
            textView.setText(result);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CameraHelper.REQUEST_IMAGE_CAPTURE) {
            loadedImage.setImageBitmap(cameraHelper.getBitmap(resultCode));
            System.out.println("-- getting a pic... set it up for screen");

            System.out.println("-------- got a pic... now Watson... in the thread");
            System.out.println(new WatsonTask().execute(resultCode));
        }

        System.out.println("------ now classify an image");
        if (requestCode == CameraHelper.REQUEST_IMAGE_CAPTURE) {
            image = cameraHelper.getFile(resultCode);
            System.out.println("image to string: " + image.toString());
        }
    }

    private VisualRecognition initVisualRecognitionService() {
        return new VisualRecognition(VisualRecognition.VERSION_DATE_2016_05_20,
                getString(R.string.visual_recognition_api_key));
    }

    private TextToSpeech initTextToSpeechService() {
        TextToSpeech service = new TextToSpeech();
        String username = getString(R.string.text_speech_username);
        String password = getString(R.string.text_speech_password);
        service.setUsernameAndPassword(username, password);
        return service;
    }

    /**
     * On request permissions result.
     *
     * @param requestCode  the request code
     * @param permissions  the permissions
     * @param grantResults the grant results
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case CameraHelper.REQUEST_PERMISSION: {
                // permission granted
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    cameraHelper.dispatchTakePictureIntent();
                }
            }

            default:
                Toast.makeText(this, "yay!", Toast.LENGTH_SHORT).show();
        }
    }

}
