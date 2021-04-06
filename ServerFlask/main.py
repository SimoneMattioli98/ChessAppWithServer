from flask import Flask, request
from flask_restful import Api, reqparse, Resource, fields, marshal_with
from flask_sqlalchemy import SQLAlchemy
from datetime import datetime
import os
import base64

#path

PATH = "dataset/"

#server init
app = Flask(__name__)
api = Api(app)

#database init
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///database.db'
db = SQLAlchemy(app)

#image put parser
image_put_parser = reqparse.RequestParser()
image_put_parser.add_argument('image', type=str, help='The pieces image converted to string', required=True)
image_put_parser.add_argument('piece_type', type=str, help='Type of the piece', required=True)
image_put_parser.add_argument('piece_color', type=str, help='Color of the piece', required=True)

#create the image model
class ImageModel(db.Model):
    id = db.Column(db.Integer, primary_key = True)
    piece_type = db.Column(db.String)
    piece_color = db.Column(db.String)
    path = db.Column(db.String)


    def __repr__(self):
        return f"Image(id = {id}, piece_type = {piece_type}, piece_color = {piece_color}, path = {path})"

#to drop the existing db and create a new one (run only once or if the db needs to be redone)
db.drop_all()
db.create_all()

#for the return statement to be in the right structure
resource_fields = {
    'id': fields.Integer,
    'piece_type': fields.String,
    'piece_color': fields.String,
    'path': fields.String
}

class Images(Resource):

    @marshal_with(resource_fields)
    def put(self):
        
        image_string = request.form.getlist('encodedImage')[0]
        piece_type = request.form.getlist('category')[0]
        piece_color = request.form.getlist('color')[0]

        now = datetime.now() 

        now = now.strftime("%d%m%Y%H%M%S")

        if(piece_type == 'empty'):
            complete_path = PATH + piece_type
            image_path = os.path.join(complete_path, piece_type + now + ".jpeg")
        else:
            complete_path = PATH + piece_color + "_" + piece_type
            image_path = os.path.join(complete_path, piece_type + piece_color + now + ".jpeg")
        

        

        f = open(image_path, "wb")
        f.write(base64.decodebytes(image_string.encode())) 

        
        image = ImageModel(piece_type = piece_type, piece_color =piece_color, path = image_path)
        db.session.add(image)
        db.session.commit()


        return image, 201

       

api.add_resource(Images, "/images")


if __name__ == "__main__":
    app.run(debug=True)