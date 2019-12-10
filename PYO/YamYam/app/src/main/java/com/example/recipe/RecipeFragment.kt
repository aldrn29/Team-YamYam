package com.example.recipe

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yamyam.R
import com.example.yamyam.searchResult.SearchResultActivity
import com.example.yamyam.searchResult.SearchResultRecipe
import com.example.yamyam.searchResult.SearchResultRecipeAdapter
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_recipelist.*

class RecipeFragment : Fragment() {

    private var mRecylerview : RecyclerView? = null
    private var linearLayoutManager : LinearLayoutManager? = null
    private var SearchResultAdapter : SearchResultRecipeAdapter? = null
    private var searchResultRecipeList =  ArrayList<RecipeSource>()

    lateinit var ref: DatabaseReference

    lateinit var showProgress: ProgressBar

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        firebaseData()
//
//    }

    //리사이클러뷰 초기화 하고 레이아웃을 초기화 해야함
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? {
        var viewInflater: View = inflater.inflate(R.layout.fragment_recipelist, container, false)


        linearLayoutManager = LinearLayoutManager(activity)
        mRecylerview = viewInflater.findViewById(R.id.recipe_list) as RecyclerView
        mRecylerview?.layoutManager = linearLayoutManager

        ref = FirebaseDatabase.getInstance().getReference().child("recipes")

        showProgress = viewInflater.findViewById(R.id.progress_bar)

        firebaseData()

        return viewInflater
    }



    private fun firebaseData() {


        val option = FirebaseRecyclerOptions.Builder<RecipeSource>()
            .setQuery(ref, RecipeSource::class.java)
            .setLifecycleOwner(this)
            .build()


        val firebaseRecyclerAdapter = object: FirebaseRecyclerAdapter<RecipeSource, MyViewHolder>(option) {


            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
                val itemView = LayoutInflater.from(context).inflate(R.layout.cardview,parent,false)
                return MyViewHolder(itemView)
            }

            override fun onBindViewHolder(holder: MyViewHolder, position: Int, model: RecipeSource) {
                val placeid = getRef(position).key.toString()

                ref.child(placeid).addValueEventListener(object: ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    //모델의 파라미터명과 같은 DB의 해당 자료를 가져온다 파라미터명 바꿀시 주의
                    override fun onDataChange(p0: DataSnapshot) {
                        showProgress.visibility = if(itemCount == 0) View.VISIBLE else View.GONE
                        holder.itemName.text = model.name
                        if (model.imageUri!!.isEmpty()) {
                            //기본 이미지
                            holder.itemImg.setImageResource(R.drawable.tomato)
                        } else {
                            Picasso.get().load(model.imageUri).into(holder.itemImg)
                        }
                        holder.itemView.setOnClickListener{
                            val intent = Intent(activity, Recipe::class.java)
                            intent.putExtra("Firebase_Image", model.imageUri)
                            intent.putExtra("Firebase_Title", model.name)
                            //TODO 재료 리스트를 받아야하는데 mutableList가 받아지지 않음
//                            intent.putStringArrayListExtra("Firebase_Materials", model.material)
                            intent.putExtra("Firebase_Description", model.description)
                            startActivity(intent)
                        }

                    }
                })
            }
        }

        mRecylerview!!.adapter = firebaseRecyclerAdapter

        firebaseRecyclerAdapter.startListening()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val editBtn: Button = view.findViewById(R.id.editBtn)

        editBtn.setOnClickListener {
            val editIntent = Intent(activity, EditRecipe::class.java)
//            startActivityForResult(editIntent,1)
            startActivity(editIntent)
        }

        var searchRecipeName : String
        searchBtn.setOnClickListener {
            searchResultRecipeList.clear()
            SearchResultAdapter = SearchResultRecipeAdapter(requireContext(), searchResultRecipeList)
            mRecylerview!!.adapter = SearchResultAdapter
            searchRecipeName = editText.text.toString()         //검색창에 작성한 레시피 이름
            SearchResultAdapter!!.notifyDataSetChanged()

            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(recipes: DataSnapshot) {
                    //작성한 이름을 firebase 에서 검색
                    searchRecipeFromFirebase(recipes, searchRecipeName)

                }
                override fun onCancelled(p0: DatabaseError) {

                }
            })
            SearchResultAdapter?.notifyDataSetChanged()
        }
    }


    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var itemName: TextView = itemView.findViewById(R.id.recipeItemName)
        internal var itemImg: ImageView = itemView.findViewById(R.id.recipeItemImg)

    }

    /*레시피 이름으로 파이어베이스에서 검색하는 함수*/
    fun searchRecipeFromFirebase(dataSnapshot: DataSnapshot, searchRecipeName : String) {
        for(dataSnapshotChild in dataSnapshot.children){
            if(dataSnapshotChild.child("name").getValue(String::class.java) == searchRecipeName){

                var materialListInFirebase  = ArrayList<String>()
                for(material in dataSnapshotChild.child("materialsList").children){
                    materialListInFirebase.add(material.getValue(String::class.java)!!)
                }
                searchResultRecipeList.add(RecipeSource(
                    dataSnapshotChild.child("description").getValue(String::class.java)!!,
                    dataSnapshotChild.child("imageUri").getValue(String::class.java)!!,
                    materialListInFirebase,
                    dataSnapshotChild.child("name").getValue(String::class.java)!!
                ))
                SearchResultAdapter?.notifyDataSetChanged()
            }
        }
    }
}
