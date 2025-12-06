import boto3
import json
import uuid

s3 = boto3.client("s3")

BUCKET_NAME = "my-unique-image-upload-bucket"

def lambda_handler(event, context):
    print("Event:", json.dumps(event))

    identity_id = None

    # 1. Cognito User Pool (JWT Authorizer)
    try:
        identity_id = event["requestContext"]["authorizer"]["jwt"]["claims"].get("sub")
    except:
        pass

    # 2. Cognito Identity Pool (Federated Identity)
    if not identity_id:
        try:
            identity_id = event["requestContext"]["identity"]["cognitoIdentityId"]
        except:
            pass

    if not identity_id:
        return {
            "statusCode": 401,
            "body": json.dumps({"error": "Unauthorized: No identityId found"})
        }

    file_id = str(uuid.uuid4()) + ".jpg"
    object_key = f"user-uploads/{identity_id}/{file_id}"

    upload_url = s3.generate_presigned_url(
        ClientMethod="put_object",
        Params={
            "Bucket": BUCKET_NAME,
            "Key": object_key,
            "ContentType": "image/jpeg"
        },
        ExpiresIn=300
    )

    return {
        "statusCode": 200,
        "headers": {
            "Access-Control-Allow-Origin": "*",
            "Content-Type": "application/json"
        },
        "body": json.dumps({
            "upload_url": upload_url,
            "file_path": object_key
        })
    }
