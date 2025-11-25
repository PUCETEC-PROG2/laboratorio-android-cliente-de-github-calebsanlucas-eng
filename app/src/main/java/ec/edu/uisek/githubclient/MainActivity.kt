package ec.edu.uisek.githubclient

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import ec.edu.uisek.githubclient.databinding.ActivityMainBinding
import ec.edu.uisek.githubclient.models.Repo
import ec.edu.uisek.githubclient.models.RepoUpdateRequest
import ec.edu.uisek.githubclient.services.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// PASO 1: Implementar la interfaz OnRepoActionListener
class MainActivity : AppCompatActivity(), OnRepoActionListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var reposAdapter: ReposAdapter


    private val githubUser = "calebsanlucas-eng"

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

    private fun setupRecyclerView() {
        // Pasar 'this' como listener al constructor del ReposAdapter
        reposAdapter = ReposAdapter(this)
        binding.repoRecyclerView.adapter = reposAdapter
    }



    private fun fetchRepositories() {
        val apiService = RetrofitClient.gitHubApiService
        val call = apiService.getRepos()

        call.enqueue(object: Callback<List<Repo>> {
            override fun onResponse(call: Call<List<Repo>>, response: Response<List<Repo>>) {
                if (response.isSuccessful) {
                    val repos = response.body()
                    if (repos != null && repos.isNotEmpty()) {
                        reposAdapter.updateRepositories(newRepos = repos)
                    } else {
                        showMessage("Usted no tiene repositorios o la lista está vacía.")
                    }
                } else {
                    val errorMsg = when (response.code()) {
                        401 -> "Error de autenticación. Revisa tu token."
                        403 -> "Recurso no permitido."
                        404 -> "Recurso no encontrado."
                        else -> "Error desconocido: ${response.code()}"
                    }
                    Log.e("MainActivity", errorMsg)
                    showMessage(errorMsg)
                }
            }

            override fun onFailure(call: Call<List<Repo>>, t: Throwable) {
                val errMsg = "Error de conexión: ${t.message}"
                Log.e("MainActivity", errMsg, t)
                showMessage(errMsg)
            }
        })
    }

    private fun showMessage(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    private fun displayNewRepo() {
        Intent(this, RepoForm::class.java).apply {
            startActivity(this)
        }
    }

    // Implementación de los métodos de la interfaz ---

    override fun onEditRepo(repo: Repo) {
        showEditRepoDialog(repo)
    }

    override fun onDeleteRepo(repo: Repo) {
        showDeleteConfirmationDialog(repo)
    }

    // --- PASO 4: Lógica para diálogos y llamadas a la API de edición/eliminación ---

    private fun showEditRepoDialog(repo: Repo) {
        val editText = EditText(this).apply {
            // Rellenamos el campo de texto con el nombre actual del repo
            setText(repo.name)
        }

        AlertDialog.Builder(this)
            .setTitle("Editar Repositorio")
            .setMessage("Introduce el nuevo nombre para el repositorio:")
            .setView(editText)
            .setPositiveButton("Guardar") { _, _ ->
                val newName = editText.text.toString()
                if (newName.isNotEmpty() && newName != repo.name) {
                    // Creamos el cuerpo de la petición con el nuevo nombre
                    val updateRequest = RepoUpdateRequest(name = newName, description = repo.description)
                    updateRepository(repo.name, updateRequest)
                } else {
                    showMessage("El nombre no puede estar vacío o ser el mismo.")
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDeleteConfirmationDialog(repo: Repo) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Eliminación")
            .setMessage("¿Estás seguro de que quieres eliminar el repositorio '${repo.name}'? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteRepository(repo.name)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun updateRepository(originalRepoName: String, updateRequest: RepoUpdateRequest) {
        val call = RetrofitClient.gitHubApiService.updateRepo(githubUser, originalRepoName, updateRequest)

        call.enqueue(object: Callback<Repo> {
            override fun onResponse(call: Call<Repo>, response: Response<Repo>) {
                if (response.isSuccessful) {
                    showMessage("Repositorio actualizado con éxito")
                    fetchRepositories() // Recarga la lista para ver los cambios
                } else {
                    Log.e("UpdateRepo", "Error: ${response.code()} - ${response.errorBody()?.string()}")
                    showMessage("Error al actualizar: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Repo>, t: Throwable) {
                Log.e("UpdateRepo", "Fallo de conexión", t)
                showMessage("Fallo en la conexión: ${t.message}")
            }
        })
    }

    private fun deleteRepository(repoName: String) {
        val call = RetrofitClient.gitHubApiService.deleteRepo(githubUser, repoName)

        call.enqueue(object: Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                // Para una petición DELETE, un código 204 (No Content) significa éxito.
                if (response.isSuccessful) {
                    showMessage("Repositorio eliminado con éxito")
                    fetchRepositories() // Recarga la lista para quitar el repo eliminado
                } else {
                    Log.e("DeleteRepo", "Error: ${response.code()} - ${response.errorBody()?.string()}")
                    showMessage("Error al eliminar: ${response.code()}. Revisa los permisos del token.")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("DeleteRepo", "Fallo de conexión", t)
                showMessage("Fallo en la conexión: ${t.message}")
            }
        })
    }
}
