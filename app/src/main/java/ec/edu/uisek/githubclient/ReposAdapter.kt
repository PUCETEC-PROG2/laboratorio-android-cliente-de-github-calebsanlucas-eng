package ec.edu.uisek.githubclient

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ec.edu.uisek.githubclient.databinding.FragmentRepoItemBinding
import ec.edu.uisek.githubclient.models.Repo

// PASO 1: Define la interfaz para comunicar acciones a la Activity/Fragment.
// Esta interfaz permitirá que el Adapter le diga a la MainActivity que se ha pulsado un botón.
interface OnRepoActionListener {
    fun onEditRepo(repo: Repo)
    fun onDeleteRepo(repo: Repo)
}

// Clase ViewHolder: No cambia mucho, solo su función bind ahora recibe el listener.
class RepoViewHolder(private val binding: FragmentRepoItemBinding) : RecyclerView.ViewHolder(binding.root) {

    // La función 'bind' ahora también necesita el listener para configurar los clics de los botones.
    fun bind(repo: Repo, listener: OnRepoActionListener) {
        // --- Tu código existente para mostrar los datos ---
        binding.repoName.text = repo.name
        binding.repoDescription.text = repo.description ?: "El repositorio no tiene descripcion"
        binding.repoLang.text = repo.language ?: "Lenguaje no especificado"
        Glide.with(binding.root.context)
            .load(repo.owner.avatarURL)
            .placeholder(R.mipmap.ic_launcher)
            .error(R.mipmap.ic_launcher)
            .circleCrop()
            .into(binding.repoOwnerImage)

        // --- PASO 2: Configurar los listeners para los botones ---
        // Usamos el ViewBinding (binding) para acceder a los botones que agregamos en el XML.
        binding.editButton.setOnClickListener {
            // Cuando se hace clic, se llama a la función correspondiente del listener,
            // pasando el 'repo' de esta fila.
            listener.onEditRepo(repo)
        }

        binding.deleteButton.setOnClickListener {
            listener.onDeleteRepo(repo)
        }
    }
}

// Clase Adapter: Ahora necesita recibir el listener para pasárselo a cada ViewHolder.
// PASO 3: Modifica el constructor para que acepte una instancia de OnRepoActionListener.
class ReposAdapter(private val listener: OnRepoActionListener) : RecyclerView.Adapter<RepoViewHolder>() {

    private var repositories: List<Repo> = emptyList()
    override fun getItemCount(): Int = repositories.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepoViewHolder {
        val binding = FragmentRepoItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RepoViewHolder(binding)
    }

    // PASO 4: Modifica onBindViewHolder para pasar el listener y el repo al método bind.
    override fun onBindViewHolder(holder: RepoViewHolder, position: Int) {
        val repo = repositories[position]
        // Llama a la función 'bind' del ViewHolder, pasando tanto el repo como el listener.
        holder.bind(repo, listener)
    }

    fun updateRepositories(newRepos: List<Repo>) {
        repositories = newRepos
        notifyDataSetChanged()
    }
}
