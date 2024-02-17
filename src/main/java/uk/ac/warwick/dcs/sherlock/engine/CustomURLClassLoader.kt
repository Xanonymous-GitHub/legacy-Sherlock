package uk.ac.warwick.dcs.sherlock.engine

import java.net.URL
import java.net.URLClassLoader


class CustomURLClassLoader(urls: Array<URL>, parent: ClassLoader) : URLClassLoader(urls, parent) {
    override fun addURL(url: URL) {
        super.addURL(url)
    }
}
