package ru.danya02.simplephonedialer

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.net.toUri
import ru.danya02.simplephonedialer.ui.theme.AppTheme


class MainActivity : ComponentActivity() {
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        // check whether the permission is granted
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("Permission", "Permission Granted")
            return
        }

        // If not, show an alert dialog
        else {
            Log.d("Permission", "Permission Denied")
            makeDialogBox()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge()

        setContent {
            AppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RootComponent(
                        dialNumber = ::dialNumber,
                    )
                }
            }
        }
    }

    fun dialNumber(number: String) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            makeDialogBox()
            return
        }


        // Use the system telephony to create a dial intent
        val dialIntent = Intent(Intent.ACTION_CALL)
        dialIntent.data = "tel:$number".toUri()

        // Start the dial intent
        try {
            startActivity(this, dialIntent, null)
        } catch (e: SecurityException) {
            // Handle the exception
            makeDialogBox()
        }
    }

    private fun makeDialogBox() {
        AlertDialog.Builder(this)
            .setCancelable(false)
            .setTitle("Dialing Permission required")
            .setMessage("This app dials phone numbers, so it requires a permission to make phone calls. Without this permission, the app cannot work.")
            .setPositiveButton("Request permission") { _, _ ->
                // Request the permission again
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CALL_PHONE),
                    1
                )
            }
            .setNeutralButton("Open settings") { _, _ ->
                openSettingsForPermissions()
            }
            .setNegativeButton("Exit app") { _, _ ->
                finish()
            }
            .show()
    }

    private fun openSettingsForPermissions() {
        val myAppSettings = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:$packageName".toUri()
        )
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT)
        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(myAppSettings)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!", modifier = modifier
    )
}


@Preview(showBackground = true)
@Composable
fun RootComponent(
    modifier: Modifier = Modifier,
    dialNumber: (String) -> Unit = {}
) {
    var dialedNumber by remember { mutableStateOf("") }


    AppTheme {
        Greeting("Android", modifier)
        Column {
            NumberField(
                number = dialedNumber
            )
            ButtonGrid(
                dialedNumber = dialedNumber,
                onUpdate = { dialedNumber = it },
                onDial = {
                    val number = dialedNumber
                    dialedNumber = ""
                    dialNumber(number)
                }
            )
        }
    }
}

@Composable
fun NumberField(number: String) {
    TextField(
        value = number,
        {},
        readOnly = true,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

@Composable
fun ButtonGrid(
    dialedNumber: String,
    onUpdate: (String) -> Unit,
    onDial: () -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Create buttons in the grid
        items(9) { index -> // Change 9 to the number of buttons you want
            Button(
                onClick = {
                    Log.d("Button", "Button $index clicked");
                    onUpdate(dialedNumber + (index + 1).toString())
                },
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .requiredHeight(64.dp)
            ) {
                Text((index + 1).toString())
            }
        }
        item {
            Button(
                onClick = {
                    Log.d("Button", "Button erase clicked");
                    // remove last digit
                    onUpdate(dialedNumber.dropLast(1))
                },
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .requiredHeight(64.dp),
                enabled = dialedNumber.isNotEmpty()
            ) {
                Text("erase")
            }
        }
        item {
            Button(
                onClick = {
                    Log.d("Button", "Button 0 clicked");
                    onUpdate(dialedNumber + "0")
                },
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .requiredHeight(64.dp)
            ) {
                Text("0")
            }
        }
        item {
            Button(
                onClick = {
                    Log.d("Button", "Button dial clicked")
                    onDial()
                },
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .requiredHeight(64.dp),
                enabled = dialedNumber.isNotEmpty()
            ) {
                Text("dial")
            }
        }
    }
}