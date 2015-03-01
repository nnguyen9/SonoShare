package com.example.sonoshare;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class MainActivity
    extends FragmentActivity
{
    private MainFragment           mainFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null)
        {
            // Add the fragment on initial activity setup
            mainFragment = new MainFragment();
            getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, mainFragment).commit();
        }
// else
// {
// // Or set the fragment from restored state info
// mainFragment =
// (MainFragment)getSupportFragmentManager().findFragmentById(
// android.R.id.content);
// }
    }
}
