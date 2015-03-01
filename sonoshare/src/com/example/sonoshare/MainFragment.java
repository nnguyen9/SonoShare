package com.example.sonoshare;

import android.widget.ImageView;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphObjectList;
import com.facebook.LoggingBehavior;
import com.facebook.Settings;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import java.util.ArrayList;
import java.util.Arrays;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainFragment
    extends Fragment
{
    private TextView               userInfoTextView;
    private LoginButton            authButton;
    private static final String    TAG            = "MainFragment";
    private Session.StatusCallback statusCallback = new SessionStatusCallback();
    private UiLifecycleHelper      uiHelper;
    private GraphUser              user;
    private TextView               userName;
    private ImageView              userPicView;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        uiHelper = new UiLifecycleHelper(getActivity(), statusCallback);
        uiHelper.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(
        LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.activity_main, container, false);

        userInfoTextView = (TextView)view.findViewById(R.id.userInfoTextView);

        authButton = (LoginButton)view.findViewById(R.id.authButton);
        authButton.setReadPermissions(Arrays.asList("public_profile"));
        authButton.setFragment(this);
        authButton.setReadPermissions(Arrays.asList(
            "user_location",
            "user_birthday",
            "user_likes"));

        return view;
    }


    private void onClickLogin()
    {
        Session session = Session.getActiveSession();
        if (!session.isOpened() && !session.isClosed())
        {
            session.openForRead(new Session.OpenRequest(this).setPermissions(
                Arrays.asList("public_profile")).setCallback(statusCallback));
        }
        else
        {
            Session
                .openActiveSession(getActivity(), this, true, statusCallback);
        }
    }


    private class SessionStatusCallback
        implements Session.StatusCallback
    {
        @Override
        public void call(
            Session session,
            SessionState state,
            Exception exception)
        {
            onSessionStateChange(session, state, exception);
        }
    }


    private void onSessionStateChange(
        Session session,
        SessionState state,
        Exception exception)
    {
        if (state.isOpened())
        {
            Log.i(TAG, "Logged in...");
            userInfoTextView.setVisibility(View.VISIBLE);

            // Request user data and show the results
            Request.newMeRequest(session, new Request.GraphUserCallback() {

                @Override
                public void onCompleted(GraphUser user, Response response)
                {
                    // Display the parsed user info
                    // userInfoTextView.setText(buildUserInfoDisplay(user));
                    if (user != null)
                    {
                        userName.setText(user.getName());
                    }
                }
            }).executeAsync();
        }
        else if (state.isClosed())
        {
            Log.i(TAG, "Logged out...");
            userInfoTextView.setVisibility(View.INVISIBLE);
        }
    }



    @Override
    public void onResume()
    {
        super.onResume();
        uiHelper.onResume();
        Session session = Session.getActiveSession();
        if (session != null && (session.isOpened() || session.isClosed()))
        {
            onSessionStateChange(session, session.getState(), null);
        }
        uiHelper.onResume();

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onPause()
    {
        super.onPause();
        uiHelper.onPause();
    }


    @Override
    public void onDestroy()
    {
        super.onDestroy();
        uiHelper.onDestroy();
    }


    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }


// ----------------- Data extracter help ----------------------

    private interface MyGraphLanguage
        extends GraphObject
    {
        // Getter for the ID field
        String getId();


        // Getter for the Name field
        String getName();
    }
}
