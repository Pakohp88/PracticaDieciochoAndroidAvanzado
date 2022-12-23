package dgtic.unam.oauth

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.ktx.Firebase
import dgtic.unam.oauth.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        validate()
        sesiones()
    }

    private fun sesiones() {
        var preferences =
            getSharedPreferences(getString(R.string.file_preferencia), Context.MODE_PRIVATE)
        var email: String? = preferences.getString("email", null)
        var provedor: String? = preferences.getString("provedor", null)

        if (email != null && provedor != null) {
            opciones(email, TipoProvedor.valueOf(provedor))
        }
    }

    private fun validate() {
        binding.updateUser.setOnClickListener {

            if (!binding.username.text.toString().isEmpty() && !binding.password.text.toString()
                    .isEmpty()
            ) {

                FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                    binding.username.text.toString(),
                    binding.password.text.toString()
                ).addOnCompleteListener {
                    if (it.isComplete) {
                        Toast.makeText(
                            binding.signin.context,
                            "Enlace con exito",
                            Toast.LENGTH_SHORT
                        ).show()
                        opciones(it.result?.user?.email ?: "", TipoProvedor.CORREO)
                    } else {
                        alert()
                    }
                }
            }
        }

        binding.loginbtn.setOnClickListener {
            if (!binding.username.text.toString().isEmpty() && !binding.password.text.toString()
                    .isEmpty()
            ) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(
                    binding.username.text.toString(),
                    binding.password.text.toString()
                ).addOnCompleteListener {
                    if(it.isSuccessful){
                        opciones(it.result?.user?.email ?: "", TipoProvedor.CORREO)
                    }else {
                        alert()
                    }
                }
            }
        }

        //Acceso con google
        iniciarActividad()
        binding.google.setOnClickListener {
            val conf= GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(
                getString(R.string.default_web_client_id)
            ).requestEmail().build()

            val clienteGoogle = GoogleSignIn.getClient(this, conf)
            clienteGoogle.signOut()

            var signIn: Intent=clienteGoogle.signInIntent
            activityResultLauncher.launch(signIn)
        }

    }

    fun iniciarActividad(){
        activityResultLauncher= registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
            if(result.resultCode== Activity.RESULT_OK){
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try{
                    val account = task.getResult(ApiException::class.java)
                    Toast.makeText(this, "Conexion exitosa", Toast.LENGTH_SHORT).show()

                    if(account != null){
                        var credenciales= GoogleAuthProvider.getCredential(account.idToken, null)
                        FirebaseAuth.getInstance().signInWithCredential(credenciales).addOnCompleteListener{
                            if(it.isSuccessful){
                                opciones(account.email?:"", TipoProvedor.GOGGLE)
                            }
                            else{
                                alert()
                            }
                        }
                    }
                }
                catch (e: ApiException){
                    Toast.makeText(this, "Error al conectar", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun alert() {
        val bulder = AlertDialog.Builder(this)
        bulder.setTitle("Mensaje")
        bulder.setMessage("Se produjo un error, contacte al provesor")
        bulder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = bulder.create()
        dialog.show()
    }

    private fun opciones(email: String, provedor: TipoProvedor) {
        var pasos = Intent(this, OpcionesActivity::class.java).apply {
            putExtra("email", email)
            putExtra("provedor", provedor.name)
        }
        startActivity(pasos)
    }
}