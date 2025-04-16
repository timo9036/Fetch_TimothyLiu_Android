package com.example.fetch_timothyliu_android.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fetch_timothyliu_android.databinding.FragmentItemListBinding
import com.example.fetch_timothyliu_android.ui.adapter.ItemAdapter
import com.example.fetch_timothyliu_android.ui.viewmodel.ItemViewModel
import com.google.android.material.snackbar.Snackbar
import androidx.lifecycle.Observer
import com.example.fetch_timothyliu_android.ui.viewmodel.ItemViewModelFactory
import com.example.fetch_timothyliu_android.data.local.AppDatabase
import com.example.fetch_timothyliu_android.data.local.LocalDataSource
import com.example.fetch_timothyliu_android.data.remote.RemoteDataSource
import com.example.fetch_timothyliu_android.data.remote.RetrofitInstance
import com.example.fetch_timothyliu_android.data.repository.ItemRepository

class ItemListFragment : Fragment() {

    private var _binding: FragmentItemListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ItemViewModel by viewModels {
        val application = requireActivity().application
        val itemDao = AppDatabase.getDatabase(application).itemDao()
        val localDataSource = LocalDataSource(itemDao)
        val remoteDataSource = RemoteDataSource(RetrofitInstance.api)
        val repository = ItemRepository(remoteDataSource, localDataSource)
        ItemViewModelFactory(repository)
    }

    private lateinit var itemAdapter: ItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentItemListBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        itemAdapter = ItemAdapter()
        binding.recyclerView.apply {
            adapter = itemAdapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }
    }

    private fun observeViewModel() {
        viewModel.items.observe(viewLifecycleOwner, Observer { items ->
            items?.let {
                itemAdapter.submitList(it)
            }
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { errorMessage ->
            errorMessage?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.onErrorMessageShown()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerView.adapter = null
        _binding = null
    }
}