/************************************************************************/
/*									*/
/*		ivy_mat3.H						*/
/*									*/
/*	Vectors and matrices for 3d arithmetic and graphics		*/
/*									*/
/************************************************************************/
/*	Copyright 1991 Brown University -- Steven P. Reiss		*/
/************************************************************************
 * Copyright 1990, 1991, 1992 Brown University, Providence, RI		*
 *									*
 * Permission to use and modify this software and its documentation	*
 * for any purpose other than its incorporation into a commercial	*
 * product is hereby granted without fee.  Permission to copy and	*
 * distribute this software and its documentation for free for		*
 * any use other than its incorporation into a commercial product	*
 * is also granted without fee, provided, however, that the		*
 * above copyright notice appear in all copies, that both that		*
 * copyright notice and this permission notice appear in supporting	*
 * documentation, that the name of Brown University not be used in	*
 * advertising or publicity pertaining to distribution of the software	*
 * without specific, written prior permission, and that Brown		*
 * University be notified by the distributor when such distributions	*
 * are done outside of his or her organization. Brown University makes	*
 * no representations about the suitability of this software for any	*
 * purpose.  It is provided "as is" without express or implied          *
 * warranty. Brown University requests notification of any		*
 * modifications to this software or its documentation. 		*
 *									*
 * Please send the name, organization, and address (postal and		*
 * electronic) of recipients of software outside of the receiving	*
 * organization to:							*
 *									*
 *	   Software Librarian						*
 *	   Computer Science Department, Box 1910			*
 *	   Brown University						*
 *	   Providence, RI 02912 					*
 *		   or							*
 *	   brusd@cs.brown.edu						*
 *									*
 * All electronic notifications will be acknowledged.			*
 *									*
 ************************************************************************/

/* RCS: $Header: /pro/spr_cvs/pro/ivy/stdlib/src/ivy_mat3.H,v 1.3 2007-01-03 03:28:15 spr Exp $ */

/************************************************************************
 *
 * $Log: ivy_mat3.H,v $
 * Revision 1.3  2007-01-03 03:28:15  spr
 * Fix typo in file.
 *
 * Revision 1.2  2006-11-09 00:33:59  spr
 * Clean up types and operations.
 *
 * Revision 1.1  2005/06/28 17:22:11  spr
 * Move stdlib from bloom to ivy for use in veld, etc.
 *
 *
 ************************************************************************/


#ifndef IVY_MAT3_ALREADY_INCLUDED
#define IVY_MAT3_ALREADY_INCLUDED


#include <ivy_datatypes.H>
#include <math.h>
#include <string.h>


namespace Ivy {



/********************************************************************************/
/*										*/
/*	Vector3 -- Three coordinate vector					*/
/*										*/
/********************************************************************************/


class Vector3 {

private:
   double	_v[3];

public:
   Vector3()					{ _v[0] = _v[1] = _v[2] = 0; }
   Vector3(int v)				{ _v[0] = _v[1] = _v[2] = v; }
   Vector3(double v)				{ _v[0] = _v[1] = _v[2] = v; }
   Vector3(double x,double y,double z)		{ _v[0] = x; _v[1] = y; _v[2] = z; }
   Vector3(double * v)				{ _v[0] = v[0]; _v[1] = v[1]; _v[2] = v[2]; }
   Vector3(const Vector3& v)			{ _v[0] = v.x(); _v[1] = v.y(); _v[2] = v.z(); }

   operator const double *() const		{ return _v; }

   double& x()					{ return _v[0]; }
   double& y()					{ return _v[1]; }
   double& z()					{ return _v[2]; }

   double x() const				{ return _v[0]; }
   double y() const				{ return _v[1]; }
   double z() const				{ return _v[2]; }

   void x(double v)				{ _v[0] = v; }
   void y(double v)				{ _v[1] = v; }
   void z(double v)				{ _v[2] = v; }

   double& operator[] (int i)			{ return _v[i]; }
   double operator[] (int i) const		{ return _v[i]; }

   void set(double x,double y,double z) 	{ _v[0] = x; _v[1] = y; _v[2] = z; }
   void set(const Vector3& v)			{ _v[0] = v.x(); _v[1] = v.y(); _v[2] = v.z(); }

   Vector3& operator = (const double v) 	{ _v[0] = _v[1] = _v[2] = v; return *this; }
   Vector3& operator = (const Vector3& v) {
      _v[0] = v.x();
      _v[1] = v.y();
      _v[2] = v.z();
      return *this;
    }

   int isZero() const				{ return _v[0] == 0 && _v[1] == 0 && _v[2] == 0; }
   int operator ==(const Vector3& v) const {
      return _v[0] == v[0] && _v[1] == v[1] && v[2] == v[2];
    }
   int operator !=(const Vector3& v) const {
      return _v[0] != v[0] || _v[1] != v[1] || v[2] != v[2];
    }

   Vector3 operator - () const			{ return Vector3(-_v[0],-_v[1],-_v[2]); }
   Vector3 operator *(double s) const		{ return Vector3(_v[0]*s,_v[1]*s,_v[2]*s); }
   void scaleBy(double s)			{ _v[0] *= s; _v[1] *= s; _v[2] *= s; }
   Vector3& operator *= (double s)		{ scaleBy(s); return *this; }
   Vector3 operator /(double s) const		{ return Vector3(_v[0]/s,_v[1]/s,_v[2]/s); }
   Vector3& operator /= (double s)		{ _v[0] /= s; _v[1] /= s; _v[2] /= s; return *this; }
   Vector3 add(const Vector3& v) const		{ return Vector3(_v[0]+v[0],_v[1]+v[1],_v[2]+v[2]); }
   Vector3 operator +(const Vector3& v) const	{ return add(v); }
   Vector3& operator += (const Vector3& v)	{ _v[0] += v[0]; _v[1] += v[1]; _v[2] += v[2]; return *this; }
   Vector3 sub(const Vector3& v) const		{ return Vector3(_v[0]-v[0],_v[1]-v[1],_v[2]-v[2]); }
   Vector3 operator -(const Vector3& v) const	{ return sub(v); }
   Vector3& operator -= (const Vector3& v)	{ _v[0] -= v[0]; _v[1] -= v[1]; _v[2] -= v[2]; return *this; }
   Vector3 operator *(const Vector3& v) const	{ return Vector3(_v[0]*v[0],_v[1]*v[1],_v[2]*v[2]); }
   Vector3& operator *= (const Vector3& v)	{ _v[0] *= v[0]; _v[1] *= v[1]; _v[2] *= v[2]; return *this; }

   double dot() const				{ return _v[0]*_v[0]+_v[1]*_v[1]+_v[2]*_v[2]; }
   double dot(const Vector3& v) const		{ return _v[0]*v.x()+_v[1]*v.y()+_v[2]*v.z(); }
   double operator & (const Vector3& v) const	{ return dot(v); }
   double length() const			{ return sqrt(dot()); }
   double distance(const Vector3& v) const	{ return sub(v).length(); }

   Vector3 cross(const Vector3& v) const {
      return Vector3(_v[1]*v.z()-_v[2]*v.y(),_v[2]*v.x()-_v[0]*v.z(),_v[0]*v.y()-_v[1]*v.x());
    }
   Vector3 operator ^(const Vector3& v) const	{ return cross(v); }
   Vector3& operator ^= (const Vector3& v)	{ Vector3 t = cross(v); set(t); return *this; }

   bool normalize() {
      double t = length();
      if (t != 0) scaleBy(1/t);
      return t != 0;
    }

   Vector3 abs() const				{ return Vector3(fabs(_v[0]),fabs(_v[1]),fabs(_v[2])); }
   void makeAbs() {
      _v[0] = fabs(_v[0]);
       _v[1] = fabs(_v[1]);
      _v[2] = fabs(_v[2]);
    }
};




/********************************************************************************/
/*										*/
/*	Matrix3 -- 4x4 matrix for 3D graphics manipulation			*/
/*										*/
/********************************************************************************/


class Matrix3 {

private:
   double _m[4][4];

public:
   Matrix3()					{ zero(); }

   void identity() {
      zero();
      for (int i = 0; i < 4; ++i) _m[i][i] = 1.0;
    }

   void zero() {
      memset(_m,0,sizeof(_m));
    }

   double& element(int i,int j) 		{ return _m[i][j]; }
   double element(int i,int j) const		{ return _m[i][j]; }
   double * operator [] (int i) 		{ return _m[i]; }
   const double * operator [] (int i) const	{ return _m[i]; }

   Matrix3& leftMult(const Matrix3& m)		{ multiply(m._m,_m); return *this; }
   Matrix3& operator *=(const Matrix3& m)	{ multiply(_m,m._m); return *this; }

   void scale(const Vector3& v) {		// create a scaling matrix
      zero();
      for (int i = 0; i < 3; ++i) _m[i][i] = v[i];
      _m[3][3] = 1.0;
    }

   void translate(const Vector3& v) {		// create a translation matrix
      identity();
      for (int i = 0; i < 3; ++i) _m[3][i] = v[i];
    }

   bool rotateDeg(const Vector3& v0,double deg) {	// create a rotation matrix
      double rad = M_PI * deg / 180.0;
      return rotateRad(v0,rad);
    }

   bool rotateRad(const Vector3& v0,double rad) {	// create a rotation matrix
      Matrix3 ry,rd,rix,riy;
      Vector3 v = v0;
      if (!v.normalize()) return false;
      double d = hypot(v[1],v[2]);
      identity();
      if (d != 0) {
	 _m[1][1] = v[2]/d;
	 _m[1][2] = v[1]/d;
	 _m[2][1] = -v[1]/d;
	 _m[2][2] = v[2]/d;
       }
      ry.identity();
      ry[0][0] = d;
      ry[0][2] = v[0];
      ry[2][0] = -v[0];
      ry[2][2] = d;

      rd.identity();
      rd[0][0] = cos(rad);
      rd[0][1] = sin(rad);
      rd[1][0] = -rd[0][1];
      rd[1][1] = rd[0][0];

      riy = ry;
      riy[0][2] = -ry[0][2];
      riy[2][0] = -ry[2][0];

      rix = *this;
      rix[1][2] = -_m[1][2];
      rix[2][1] = -_m[2][1];

      multiply(_m,ry._m);
      multiply(_m,rd._m);
      multiply(_m,riy._m);
      multiply(_m,rix._m);

      return true;
    }

public:
   friend Vector3 operator *(const Vector3& v,const Matrix3& m) {
      Vector3 r;

      for (int i = 0; i < 3; ++i) {
	 double x = 0;
	 for (int j = 0; j < 3; ++j) x += v[j]*m[j][i];
	 r[i] = x + m[3][i];
       }

      return r;
    }

   friend Vector3 operator *(const Matrix3& m,const Vector3& v) {
      Vector3 r;

      for (int i = 0; i < 3; ++i) {
	 double x = 0;
	 for (int j = 0; j < 3; ++j) x += m[i][j]*v[j];
	 r[i] = x + m[i][3];
       }

      return r;
    }

private:
   void multiply(const double m1[4][4],const double m2[4][4]) {
      double r[4][4];

      for (int i = 0; i < 4; ++i) {
	 for (int j = 0; j < 4; ++j) {
	    double v = 0;
	    for (int k = 0; k < 4; ++k) v += m1[i][k] * m2[k][j];
	    r[i][j] = v;
	  }
       }

      for (int i = 0; i < 4; ++i) {
	 for (int j = 0; j < 4; ++j) _m[i][j] = r[i][j];
       }
    }

};



}	// end of namespace Ivy


#endif


/* end of ivy_mat3.H */
