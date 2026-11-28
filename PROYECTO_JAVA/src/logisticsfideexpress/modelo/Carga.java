    /*
     * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
     * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
     */
    package logisticsfideexpress.modelo;

    /**
     *
     * @author JETLI
     */
    public class Carga {

        private String descripcion;
        private double pesoKg;
        private double volumen;

        public Carga(String descripcion, double pesoKg, double volumen) {
            this.descripcion = descripcion;
            this.pesoKg = pesoKg;
            this.volumen = volumen;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }

        public double getPesoKg() {
            return pesoKg;
        }

        public void setPesoKg(double pesoKg) {
            this.pesoKg = pesoKg;
        }

        public double getVolumen() {
            return volumen;
        }

        public void setVolumen(double volumen) {
            this.volumen = volumen;
        }
    }
