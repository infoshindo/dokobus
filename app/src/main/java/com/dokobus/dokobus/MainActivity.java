package com.dokobus.dokobus;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);

        // DrawerToggle
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // NavigationView Listener
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.all_bus)
        {
            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra("rosen_id", "0");
            startActivity(intent);
        }
        else if (id == R.id.rosen1)
        {
            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra("rosen_id", "1");
            startActivity(intent);
        }
        else if (id == R.id.rosen2)
        {
            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra("rosen_id", "2");
            startActivity(intent);
        }
        else if (id == R.id.rosen3)
        {
            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra("rosen_id", "3");
            startActivity(intent);
        }
        else if (id == R.id.rosen4)
        {
            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra("rosen_id", "4");
            startActivity(intent);
        }
        else if (id == R.id.rosen5)
        {
            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra("rosen_id", "5");
            startActivity(intent);
        }
        else if (id == R.id.rosen6)
        {
            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra("rosen_id", "6");
            startActivity(intent);
        }
        else if (id == R.id.rosen7)
        {
            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra("rosen_id", "7");
            startActivity(intent);
        }
        else if (id == R.id.rosen8)
        {
            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra("rosen_id", "8");
            startActivity(intent);
        }
        else if (id == R.id.rosen9)
        {
            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra("rosen_id", "9");
            startActivity(intent);
        }
        else if (id == R.id.rosen10)
        {
            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra("rosen_id", "10");
            startActivity(intent);
        }
        else if (id == R.id.rosen11)
        {
            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra("rosen_id", "11");
            startActivity(intent);
        }
        else if (id == R.id.rosen12)
        {
            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra("rosen_id", "12");
            startActivity(intent);
        }
        else if (id == R.id.rosen13)
        {
            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra("rosen_id", "13");
            startActivity(intent);
        }
        else if (id == R.id.rosen14)
        {
            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra("rosen_id", "14");
            startActivity(intent);
        }
        else if (id == R.id.rosen15)
        {
            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra("rosen_id", "15");
            startActivity(intent);
        }
        else if (id == R.id.rosen16)
        {
            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra("rosen_id", "16");
            startActivity(intent);
        }
        else if (id == R.id.rosen17)
        {
            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra("rosen_id", "17");
            startActivity(intent);
        }
        else if (id == R.id.rosen99)
        {
            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra("rosen_id", "99");
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}