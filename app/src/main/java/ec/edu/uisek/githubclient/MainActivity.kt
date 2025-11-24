package ec.edu.uisek.githubclient

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ec.edu.uisek.githubclient.databinding.ActivityMainBinding
import ec.edu.uisek.githubclient.models.Repo
import ec.edu.uisek.githubclient.services.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var reposAdapter: ReposAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.newRepoFab.setOnClickListener {
            displayNewRepo()
        }


    }

    override fun onResume() {
        super.onResume()
        setupRecyclerView()
        fetchRepositories()
    }

    private fun setupRecyclerView(){
        reposAdapter = ReposAdapter()
        binding.repoRecyclerView.adapter = reposAdapter

    }

    private fun fetchRepositories() {
        val apiService = RetrofitClient.gitHubApiService
        val call = apiService.getRepos()

        call.enqueue(object: Callback<List<Repo>> {
            override fun onResponse(call: Call<List<Repo>?>, response: Response<List<Repo>?>) {
                if (response.isSuccessful) {
                    val repos = response.body()
                    if (repos != null && repos.isNotEmpty()) {
                        reposAdapter.updateRepositories(newRepos = repos)
                    } else {
                        // VACÍA
                        showMessage(msg = "Usted no tiene repositorios")
                    }
                } else {
                    val errorMsg = when (response.code()) {
                        401 -> "Error de autenticación"
                        403 -> "Recurso no permitido"
                        404 -> "Recurso no encontrado"
                        else -> "Error desconocido: ${response.code()}"
                    }
                    Log.e("MainActivity",  errorMsg)
                    showMessage(errorMsg)
                }
            }

            override fun onFailure(call: Call<List<Repo>?>, t: Throwable) {
                val errMsg = "Error de conexión: ${t.message}"
                Log.e("MainActivity", errMsg, t)
                showMessage(errMsg)
            }
        })
    }


    private fun showMessage (msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    private fun displayNewRepo(){
        Intent(this, RepoForm::class.java).apply {
            startActivity(this)
        }
    }
}
