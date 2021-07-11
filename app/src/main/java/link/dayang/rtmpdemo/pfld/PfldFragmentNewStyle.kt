package link.dayang.rtmpdemo.pfld

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.convertTo
import androidx.fragment.app.Fragment
import com.sample.tracking.FaceOverlapFragment
import link.dayang.rtmpdemo.R

class PfldFragmentNewStyle : Fragment() {

    private lateinit var root: ViewGroup

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_pfld_new_style, container, false)
        root = view as ViewGroup
        initViews()
        return view
    }

    private fun initViews() {
        val overlapFragment = FaceOverlapFragment()
        val tran = parentFragmentManager.beginTransaction()
        tran.replace(R.id.pfld_container, overlapFragment)
        tran.commit()


    }
}