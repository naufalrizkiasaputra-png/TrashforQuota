package com.trashforquota.ProjectPBO.service;

import com.trashforquota.ProjectPBO.model.ItemSampah;
import com.trashforquota.ProjectPBO.repository.ItemSampahRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemSampahService {

    @Autowired
    private ItemSampahRepository itemSampahRepository;

    // Ambil semua jenis sampah untuk ditampilkan di dropdown user & tabel admin
    public List<ItemSampah> getAllItemSampah() {
        return itemSampahRepository.findAll();
    }

    // Simpan atau update jenis sampah (untuk fitur Admin)
    public ItemSampah saveItemSampah(ItemSampah itemSampah) {
        return itemSampahRepository.save(itemSampah);
    }

    // Ambil 1 sampah berdasarkan ID (untuk edit data di Admin)
    public ItemSampah getItemSampahById(Long id) {
        return itemSampahRepository.findById(id).orElse(null);
    }

    // Hapus jenis sampah (untuk fitur Admin)
    public void deleteItemSampah(Long id) {
        itemSampahRepository.deleteById(id);
    }
}